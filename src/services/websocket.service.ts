import { Client } from '@stomp/stompjs'
import type { IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { ENV } from '@config/environment'
import { tokenStorage } from '@utils/token.storage'

type SubscriptionCallback = (message: IMessage) => void

class WebSocketService {
  private client: Client | null = null
  private subscriptions: Map<string, ReturnType<Client['subscribe']>> = new Map()

  // Pending subscriptions requested before connection was ready
  private pendingSubscriptions: Array<{
    destination: string
    callback: SubscriptionCallback
  }> = []

  private onConnectedCallbacks: Array<() => void> = []

  connect(onConnected?: () => void): void {
    if (onConnected) this.onConnectedCallbacks.push(onConnected)

    this.client = new Client({
      webSocketFactory: () => new SockJS(ENV.WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${tokenStorage.getAccessToken() ?? ''}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        // Flush all pending subscriptions
        this.pendingSubscriptions.forEach(({ destination, callback }) => {
          const subscription = this.client!.subscribe(destination, callback)
          this.subscriptions.set(destination, subscription)
        })
        this.pendingSubscriptions = []

        // Run onConnected callbacks
        this.onConnectedCallbacks.forEach((cb) => cb())
        this.onConnectedCallbacks = []
      },
      onDisconnect: () => {
        this.subscriptions.clear()
      },
    })

    this.client.activate()
  }

  disconnect(): void {
    this.client?.deactivate()
    this.client = null
    this.subscriptions.clear()
    this.pendingSubscriptions = []
    this.onConnectedCallbacks = []
  }

  /**
   * Subscribe to a destination.
   * If WebSocket is not yet connected, queues the subscription
   * and executes it automatically once connected.
   */
  subscribe(destination: string, callback: SubscriptionCallback): void {
    if (this.client?.connected) {
      const subscription = this.client.subscribe(destination, callback)
      this.subscriptions.set(destination, subscription)
    } else {
      // Queue for when connection is ready
      this.pendingSubscriptions.push({ destination, callback })
    }
  }

  unsubscribe(destination: string): void {
    this.subscriptions.get(destination)?.unsubscribe()
    this.subscriptions.delete(destination)

    // Also remove from pending if not yet subscribed
    this.pendingSubscriptions = this.pendingSubscriptions.filter(
      (p) => p.destination !== destination,
    )
  }

  get isConnected(): boolean {
    return this.client?.connected ?? false
  }
}

export const websocketService = new WebSocketService()

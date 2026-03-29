import { Client } from '@stomp/stompjs'
import type { IMessage } from '@stomp/stompjs'
import SockJS from 'sockjs-client'
import { ENV } from '@config/environment'
import { tokenStorage } from '@utils/token.storage'

type SubscriptionCallback = (message: IMessage) => void

class WebSocketService {
  private client: Client | null = null
  private subscriptions: Map<string, ReturnType<Client['subscribe']>> = new Map()

  connect(onConnected?: () => void): void {
    this.client = new Client({
      webSocketFactory: () => new SockJS(ENV.WS_URL),
      connectHeaders: {
        Authorization: `Bearer ${tokenStorage.getAccessToken() ?? ''}`,
      },
      reconnectDelay: 5000,
      onConnect: () => {
        onConnected?.()
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
  }

  subscribe(destination: string, callback: SubscriptionCallback): void {
    if (!this.client?.connected) return

    const subscription = this.client.subscribe(destination, callback)
    this.subscriptions.set(destination, subscription)
  }

  unsubscribe(destination: string): void {
    this.subscriptions.get(destination)?.unsubscribe()
    this.subscriptions.delete(destination)
  }

  get isConnected(): boolean {
    return this.client?.connected ?? false
  }
}

export const websocketService = new WebSocketService()
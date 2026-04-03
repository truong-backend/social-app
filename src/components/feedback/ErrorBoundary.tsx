import { Component, type ReactNode } from 'react'

interface ErrorBoundaryProps {
  children:  ReactNode
  fallback?: ReactNode
}

interface ErrorBoundaryState {
  hasError: boolean
  error:    Error | null
}

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  constructor(props: ErrorBoundaryProps) {
    super(props)
    this.state = { hasError: false, error: null }
  }

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { hasError: true, error }
  }

  componentDidCatch(error: Error, info: { componentStack: string }) {
    console.error('[ErrorBoundary]', error, info.componentStack)
  }

  render() {
    if (this.state.hasError) {
      return (
        this.props.fallback ?? (
          <div className="flex flex-col items-center justify-center gap-4 p-10 rounded-2xl bg-surface-container-low text-center">
            <span className="text-4xl">⚠️</span>
            <h2 className="text-lg font-bold text-on-surface font-headline">
              Đã xảy ra lỗi
            </h2>
            <p className="text-sm text-on-surface-variant max-w-xs">
              {this.state.error?.message ?? 'Lỗi không xác định'}
            </p>
            <button
              className="px-6 py-2.5 rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary text-sm font-bold shadow-md shadow-primary/20 active:scale-95 transition-all"
              onClick={() => this.setState({ hasError: false, error: null })}
            >
              Thử lại
            </button>
          </div>
        )
      )
    }

    return this.props.children
  }
}
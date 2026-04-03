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
          <div className="error-boundary">
            <h2 className="error-boundary__title">Đã xảy ra lỗi</h2>
            <p className="error-boundary__message">
              {this.state.error?.message ?? 'Lỗi không xác định'}
            </p>
            <button
              className="error-boundary__retry-btn"
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
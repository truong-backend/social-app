import type { ButtonHTMLAttributes } from 'react'
import { clsx } from 'clsx'
import { Spinner } from '@components/feedback/Spinner'

type ButtonVariant = 'primary' | 'secondary' | 'danger' | 'ghost'
type ButtonSize    = 'sm' | 'md' | 'lg'

interface ButtonProps extends ButtonHTMLAttributes<HTMLButtonElement> {
  variant?:   ButtonVariant
  size?:      ButtonSize
  isLoading?: boolean
  fullWidth?: boolean
}

export const Button = ({
  children,
  variant    = 'primary',
  size       = 'md',
  isLoading  = false,
  fullWidth  = false,
  disabled,
  className,
  ...rest
}: ButtonProps) => {
  return (
    <button
      className={clsx(
        'btn',
        `btn--${variant}`,
        `btn--${size}`,
        fullWidth && 'btn--full-width',
        className,
      )}
      disabled={disabled || isLoading}
      {...rest}
    >
      {isLoading ? <Spinner size="sm" /> : children}
    </button>
  )
}
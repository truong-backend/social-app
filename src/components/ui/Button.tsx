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

const VARIANT_CLASSES: Record<ButtonVariant, string> = {
  primary:   'bg-gradient-to-br from-primary to-primary-container text-on-primary shadow-lg shadow-primary/20 hover:opacity-90',
  secondary: 'bg-surface-container-high text-primary hover:bg-surface-container-highest',
  danger:    'bg-error-container text-on-error hover:opacity-90',
  ghost:     'bg-transparent text-on-surface-variant hover:bg-surface-container-low',
}

const SIZE_CLASSES: Record<ButtonSize, string> = {
  sm: 'px-4 py-1.5 text-xs rounded-full',
  md: 'px-6 py-3 text-sm rounded-full',
  lg: 'px-8 py-4 text-base rounded-full',
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
        'inline-flex items-center justify-center font-bold transition-all duration-200 active:scale-95',
        'disabled:opacity-50 disabled:pointer-events-none',
        VARIANT_CLASSES[variant],
        SIZE_CLASSES[size],
        fullWidth && 'w-full',
        className,
      )}
      disabled={disabled || isLoading}
      {...rest}
    >
      {isLoading ? <Spinner size="sm" /> : children}
    </button>
  )
}
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
  primary:   'bg-gradient-to-r from-primary to-primary-container text-on-primary shadow-lg shadow-primary/20 hover:scale-[1.02] active:scale-[0.98]',
  secondary: 'bg-surface-container-lowest text-on-surface border border-outline-variant/20 shadow-sm hover:bg-surface-container-low',
  danger:    'bg-red-50 text-error border border-red-100 hover:bg-red-100',
  ghost:     'bg-transparent text-primary hover:bg-primary/5',
}

const SIZE_CLASSES: Record<ButtonSize, string> = {
  sm: 'px-4 py-1.5 text-xs rounded-full',
  md: 'px-6 py-3 text-sm rounded-xl',
  lg: 'px-8 py-4 text-base rounded-xl',
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
        'inline-flex items-center justify-center font-bold transition-all duration-200',
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

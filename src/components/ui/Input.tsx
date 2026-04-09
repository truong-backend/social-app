import { forwardRef, type InputHTMLAttributes } from 'react'
import { clsx } from 'clsx'

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
  label?:        string
  errorMessage?: string
  helperText?:   string
}

export const Input = forwardRef<HTMLInputElement, InputProps>(
  ({ label, errorMessage, helperText, className, id, ...rest }, ref) => {
    const inputId = id ?? label?.toLowerCase().replace(/\s+/g, '-')

    return (
      <div className="flex flex-col gap-1.5">
        {label && (
          <label
            className="text-sm font-semibold text-on-surface ml-1"
            htmlFor={inputId}
          >
            {label}
          </label>
        )}

        <input
          ref={ref}
          id={inputId}
          className={clsx(
            'w-full px-5 py-4 bg-surface-container-lowest border-0 rounded-xl',
            'text-sm text-on-surface placeholder:text-outline-variant shadow-sm',
            'focus:ring-2 focus:ring-primary-fixed outline-none transition-all',
            errorMessage && 'ring-2 ring-error',
            className,
          )}
          aria-invalid={!!errorMessage}
          aria-describedby={errorMessage ? `${inputId}-error` : undefined}
          {...rest}
        />

        {errorMessage && (
          <span
            id={`${inputId}-error`}
            className="text-xs text-error font-medium ml-1"
            role="alert"
          >
            {errorMessage}
          </span>
        )}

        {helperText && !errorMessage && (
          <span className="text-xs text-on-surface-variant ml-1">{helperText}</span>
        )}
      </div>
    )
  },
)

Input.displayName = 'Input'

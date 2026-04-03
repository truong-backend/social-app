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
            className="text-sm font-semibold text-on-surface"
            htmlFor={inputId}
          >
            {label}
          </label>
        )}

        <input
          ref={ref}
          id={inputId}
          className={clsx(
            'w-full px-4 py-3 rounded-xl bg-surface-container-low border-none',
            'text-sm text-on-surface placeholder:text-on-surface-variant/60',
            'focus:ring-2 focus:ring-primary/20 outline-none transition-all',
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
            className="text-xs text-error font-medium"
            role="alert"
          >
            {errorMessage}
          </span>
        )}

        {helperText && !errorMessage && (
          <span className="text-xs text-on-surface-variant">{helperText}</span>
        )}
      </div>
    )
  },
)

Input.displayName = 'Input'
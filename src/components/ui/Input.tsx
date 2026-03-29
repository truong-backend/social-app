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
      <div className="input-wrapper">
        {label && (
          <label className="input-wrapper__label" htmlFor={inputId}>
            {label}
          </label>
        )}

        <input
          ref={ref}
          id={inputId}
          className={clsx(
            'input-wrapper__input',
            errorMessage && 'input-wrapper__input--error',
            className,
          )}
          aria-invalid={!!errorMessage}
          aria-describedby={errorMessage ? `${inputId}-error` : undefined}
          {...rest}
        />

        {errorMessage && (
          <span id={`${inputId}-error`} className="input-wrapper__error" role="alert">
            {errorMessage}
          </span>
        )}

        {helperText && !errorMessage && (
          <span className="input-wrapper__helper">{helperText}</span>
        )}
      </div>
    )
  },
)

Input.displayName = 'Input'
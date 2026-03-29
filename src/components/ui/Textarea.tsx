import { forwardRef, type TextareaHTMLAttributes } from 'react'
import { clsx } from 'clsx'

interface TextareaProps extends TextareaHTMLAttributes<HTMLTextAreaElement> {
  label?:        string
  errorMessage?: string
  helperText?:   string
}

export const Textarea = forwardRef<HTMLTextAreaElement, TextareaProps>(
  ({ label, errorMessage, helperText, className, id, ...rest }, ref) => {
    const textareaId = id ?? label?.toLowerCase().replace(/\s+/g, '-')

    return (
      <div className="textarea-wrapper">
        {label && (
          <label className="textarea-wrapper__label" htmlFor={textareaId}>
            {label}
          </label>
        )}

        <textarea
          ref={ref}
          id={textareaId}
          className={clsx(
            'textarea-wrapper__textarea',
            errorMessage && 'textarea-wrapper__textarea--error',
            className,
          )}
          aria-invalid={!!errorMessage}
          {...rest}
        />

        {errorMessage && (
          <span className="textarea-wrapper__error" role="alert">
            {errorMessage}
          </span>
        )}

        {helperText && !errorMessage && (
          <span className="textarea-wrapper__helper">{helperText}</span>
        )}
      </div>
    )
  },
)

Textarea.displayName = 'Textarea'
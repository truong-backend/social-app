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
      <div className="flex flex-col gap-1.5">
        {label && (
          <label
            className="text-sm font-semibold text-on-surface ml-1"
            htmlFor={textareaId}
          >
            {label}
          </label>
        )}

        <textarea
          ref={ref}
          id={textareaId}
          className={clsx(
            'w-full px-5 py-4 rounded-xl bg-surface-container-lowest border-0',
            'text-sm text-on-surface placeholder:text-outline-variant shadow-sm',
            'focus:ring-2 focus:ring-primary-fixed outline-none transition-all resize-none',
            errorMessage && 'ring-2 ring-error',
            className,
          )}
          aria-invalid={!!errorMessage}
          aria-describedby={errorMessage ? `${textareaId}-error` : undefined}
          {...rest}
        />

        {errorMessage && (
          <span
            id={`${textareaId}-error`}
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

Textarea.displayName = 'Textarea'

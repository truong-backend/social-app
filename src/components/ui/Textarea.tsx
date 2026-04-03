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
            className="text-sm font-semibold text-on-surface"
            htmlFor={textareaId}
          >
            {label}
          </label>
        )}

        <textarea
          ref={ref}
          id={textareaId}
          className={clsx(
            'w-full px-4 py-3 rounded-xl bg-surface-container-low border-none',
            'text-sm text-on-surface placeholder:text-on-surface-variant/60',
            'focus:ring-2 focus:ring-primary/20 outline-none transition-all resize-none',
            errorMessage && 'ring-2 ring-error',
            className,
          )}
          aria-invalid={!!errorMessage}
          {...rest}
        />

        {errorMessage && (
          <span className="text-xs text-error font-medium" role="alert">
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

Textarea.displayName = 'Textarea'
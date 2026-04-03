import { clsx } from 'clsx'

interface SpinnerProps {
  size?:      'sm' | 'md' | 'lg'
  className?: string
}

const SIZE_CLASSES = {
  sm: 'w-4 h-4 border-2',
  md: 'w-6 h-6 border-2',
  lg: 'w-8 h-8 border-[3px]',
}

export const Spinner = ({ size = 'md', className }: SpinnerProps) => (
  <span
    className={clsx(
      'inline-block rounded-full border-primary/30 border-t-primary animate-spin',
      SIZE_CLASSES[size],
      className,
    )}
    role="status"
    aria-label="Đang tải"
  />
)
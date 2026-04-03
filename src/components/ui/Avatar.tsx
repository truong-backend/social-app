import { clsx } from 'clsx'

interface AvatarProps {
  src?:       string | null
  alt:        string
  size?:      'xs' | 'sm' | 'md' | 'lg' | 'xl'
  className?: string
}

const SIZE_CLASSES = {
  xs: 'w-6 h-6 text-[10px]',
  sm: 'w-8 h-8 text-xs',
  md: 'w-10 h-10 text-sm',
  lg: 'w-14 h-14 text-base',
  xl: 'w-24 h-24 text-2xl',
}

export const Avatar = ({ src, alt, size = 'md', className }: AvatarProps) => {
  if (!src) {
    return (
      <div
        className={clsx(
          'rounded-full flex items-center justify-center font-bold',
          'bg-gradient-to-br from-primary to-primary-container text-on-primary',
          SIZE_CLASSES[size],
          className,
        )}
        aria-label={alt}
      >
        {alt.charAt(0).toUpperCase()}
      </div>
    )
  }

  return (
    <img
      src={src}
      alt={alt}
      className={clsx(
        'rounded-full object-cover',
        SIZE_CLASSES[size],
        className,
      )}
    />
  )
}
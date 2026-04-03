import { clsx } from 'clsx'

interface AvatarProps {
  src?:       string | null
  alt:        string
  size?:      'xs' | 'sm' | 'md' | 'lg' | 'xl'
  className?: string
}

const SIZE_CLASSES = {
  xs: 'avatar--xs',
  sm: 'avatar--sm',
  md: 'avatar--md',
  lg: 'avatar--lg',
  xl: 'avatar--xl',
}

export const Avatar = ({ src, alt, size = 'md', className }: AvatarProps) => {
  if (!src) {
    return (
      <div
        className={clsx('avatar', 'avatar--placeholder', SIZE_CLASSES[size], className)}
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
      className={clsx('avatar', SIZE_CLASSES[size], className)}
    />
  )
}
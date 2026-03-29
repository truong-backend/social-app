interface UserAvatarProps {
  src: string | null
  username: string
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

const SIZE_MAP = { sm: 32, md: 44, lg: 80 }

export const UserAvatar = ({ src, username, size = 'md', className }: UserAvatarProps) => {
  const px = SIZE_MAP[size]

  return (
    <img
      src={src ?? '/default-avatar.png'}
      alt={username}
      width={px}
      height={px}
      className={`user-avatar user-avatar--${size} ${className ?? ''}`}
      style={{ borderRadius: '50%', objectFit: 'cover' }}
    />
  )
}
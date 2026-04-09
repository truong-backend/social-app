interface UserAvatarProps {
  src: string | null
  username: string
  size?: 'sm' | 'md' | 'lg'
  className?: string
}

const SIZE_MAP = {
  sm: 'w-8 h-8 text-xs',
  md: 'w-11 h-11 text-sm',
  lg: 'w-20 h-20 text-2xl',
}

export const UserAvatar = ({ src, username, size = 'md', className }: UserAvatarProps) => {
  if (src) {
    return (
      <img
        src={src}
        alt={username}
        className={`rounded-full object-cover ${SIZE_MAP[size]} ${className ?? ''}`}
      />
    )
  }

  return (
    <div
      className={`rounded-full bg-gradient-to-br from-primary to-primary-container text-on-primary flex items-center justify-center font-bold ${SIZE_MAP[size]} ${className ?? ''}`}
    >
      {username.charAt(0).toUpperCase()}
    </div>
  )
}

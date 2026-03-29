import { formatDistanceToNow, format, parseISO } from 'date-fns'

export const formatRelativeTime = (dateString: string): string =>
  formatDistanceToNow(parseISO(dateString), { addSuffix: true })

export const formatFullDate = (dateString: string): string =>
  format(parseISO(dateString), 'dd/MM/yyyy HH:mm')

export const formatDateOnly = (dateString: string): string =>
  format(parseISO(dateString), 'dd/MM/yyyy')
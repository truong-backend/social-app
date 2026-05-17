/**
 * Chuyển URL file từ backend (http://localhost:2003/v1/files/<id>)
 * về dạng tương đối (/v1/files/<id>) để đi qua Next.js proxy,
 * tránh CORS / Mixed Content.
 *
 * Nếu đã là URL tương đối hoặc null thì giữ nguyên.
 */
export function normalizeFileUrl(url) {
  if (!url) return null;
  // Đã là relative path → giữ nguyên
  if (url.startsWith("/")) return url;
  try {
    const { pathname } = new URL(url);
    return pathname; // → "/v1/files/<id>"
  } catch {
    return url;
  }
}
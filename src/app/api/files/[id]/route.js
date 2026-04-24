/**
 * /api/files/[id] — proxy server-side để tránh Mixed Content.
 *
 * Browser (HTTPS) → GET /v1/files/<id>
 *   → rewrite (next.config.mjs) → /api/files/<id>   (Next.js server)
 *   → fetch nội bộ → BACKEND_INTERNAL_URL/v1/files/<id>  (container-to-container)
 *   → stream về browser
 *
 * Tại sao dùng BACKEND_INTERNAL_URL thay vì NEXT_PUBLIC_API_URL?
 * - NEXT_PUBLIC_API_URL = https://mangxahoi.deploy-my-project.site/api
 *   → server-side gọi ra ngoài internet rồi vào lại qua Nginx → chậm, dễ lỗi TLS
 * - BACKEND_INTERNAL_URL = http://social-api:2003
 *   → gọi thẳng container BE trong cùng Docker network → nhanh, ổn định
 */
export async function GET(request, { params }) {
  const { id } = await params;

  if (!id) {
    return new Response('Missing file id', { status: 400 });
  }

  // BACKEND_INTERNAL_URL: server-side only (không NEXT_PUBLIC_)
  // Trỏ thẳng tới container BE qua Docker internal network
  // Fallback về NEXT_PUBLIC_API_URL cho môi trường dev local
  const backendBase =
    process.env.BACKEND_INTERNAL_URL ||
    process.env.NEXT_PUBLIC_API_URL ||
    'http://localhost:2003';

  const fileUrl = `${backendBase}/v1/files/${encodeURIComponent(id)}`;

  try {
    const backendResponse = await fetch(fileUrl, {
      next: { revalidate: 3600 },
    });

    if (!backendResponse.ok) {
      return new Response('File not found', { status: backendResponse.status });
    }

    const contentType =
      backendResponse.headers.get('Content-Type') || 'application/octet-stream';
    const contentDisposition =
      backendResponse.headers.get('Content-Disposition') || '';

    return new Response(backendResponse.body, {
      status: 200,
      headers: {
        'Content-Type': contentType,
        'Content-Disposition': contentDisposition,
        'Cache-Control': 'public, max-age=3600, immutable',
      },
    });
  } catch (err) {
    console.error(`[/api/files/${id}] Lỗi fetch backend:`, err);
    return new Response('Internal Server Error', { status: 500 });
  }
}
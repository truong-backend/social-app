/**
 * /api/files/[id] — proxy server-side để tránh Mixed Content.
 *
 * Browser (HTTPS/HTTP) → GET /v1/files/<id>
 *   → rewrite (next.config.mjs) → /api/files/<id>
 *   → fetch server-side → backend:2003/v1/files/<id>
 *   → stream về browser
 */
export async function GET(request, { params }) {
  const { id } = await params;

  if (!id) {
    return new Response('Missing file id', { status: 400 });
  }

  const backendBase = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:2003';
  const fileUrl = `${backendBase}/v1/files/${encodeURIComponent(id)}`;

  try {
    const backendResponse = await fetch(fileUrl, {
      next: { revalidate: 3600 },
    });

    if (!backendResponse.ok) {
      return new Response('File not found', { status: backendResponse.status });
    }

    const contentType = backendResponse.headers.get('Content-Type') || 'application/octet-stream';
    const contentDisposition = backendResponse.headers.get('Content-Disposition') || '';

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

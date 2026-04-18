/** @type {import('next').NextConfig} */
const nextConfig = {
  reactStrictMode: false,

  // Cần cho Docker — tạo ra folder .next/standalone chứa server.js tối giản
  output: 'standalone',

  images: {
    domains: [
      'pngdownload.io',
      'picsum.photos',
      'localhost',
      'pocpoc.online',
      'api.pocpoc.online',
    ],
    remotePatterns: [
      {
        // FIX 1: backend runs on port 2003, not 80.
        // Without this entry Next.js /_next/image returns 400 for all
        // avatar/post images served from localhost:2003.
        protocol: 'http',
        hostname: 'localhost',
        port: '2003',
        pathname: '/v1/files/**',
      },
      {
        protocol: 'http',
        hostname: 'localhost',
        port: '80',
        pathname: '/v1/files/**',
      },
      {
        protocol: 'https',
        hostname: 'api.pocpoc.online',
        pathname: '/v1/files/**',
      },
    ],
    formats: ['image/webp', 'image/avif'],
    minimumCacheTTL: 3600,
  },

  compiler: {
    removeConsole: process.env.NODE_ENV === 'production',
  },

  env: {
    CUSTOM_KEY: process.env.CUSTOM_KEY,
  },

  /**
   * FIX 2: Rewrites — proxy backend file URLs through Next.js server.
   *
   * The Spring Boot backend embeds the full URL into every API response
   * (e.g. profilePictureUrl = "http://localhost:2003/v1/files/<id>").
   * When the app is deployed on Vercel (HTTPS) browsers block these as
   * "Mixed Content" and the service worker logs "SW: Skipping external URL".
   *
   * These rewrites transparently redirect:
   *   GET /v1/files/<id>   =>   our server-side /api/files/<id> proxy
   *
   * The proxy route (/src/app/api/files/[id]/route.js) fetches the file
   * from the backend on the server and streams it back over HTTPS,
   * eliminating both the Mixed Content error and the SW skip.
   */
  async rewrites() {
    return [
      {
        source: '/v1/files/:id',
        destination: '/api/files/:id',
      },
    ];
  },

  webpack: (config, { isServer }) => {
    if (!isServer) {
      config.resolve.fallback = {
        fs: false,
        crypto: false,
      };
    }

    config.module.rules.push({
      test: /\.(png|jpe?g|gif|svg|eot|ttf|woff|woff2)$/i,
      type: 'asset',
    });

    return config;
  },

  async headers() {
    return [
      {
        source: '/(.*)',
        headers: [
          { key: 'X-Frame-Options', value: 'DENY' },
          { key: 'X-Content-Type-Options', value: 'nosniff' },
          { key: 'Referrer-Policy', value: 'origin-when-cross-origin' },
        ],
      },
    ];
  },

  typescript: {
    ignoreBuildErrors: false,
  },
  eslint: {
    ignoreDuringBuilds: false,
  },
  trailingSlash: false,
  poweredByHeader: false,
  compress: true,
};

export default nextConfig;

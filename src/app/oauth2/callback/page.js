"use client";

import { useEffect, Suspense } from "react";
import { useSearchParams } from "next/navigation";
import { setAuthToken } from "@/utils/axios";
import { jwtDecode } from "jwt-decode";
import api from "@/utils/axios";

function OAuth2CallbackContent() {
  const searchParams = useSearchParams();

  useEffect(() => {
    const handleCallback = async () => {
      const accessToken = searchParams.get("accessToken");

      if (!accessToken) {
        window.location.href = "/register?error=google_login_failed";
        return;
      }

      try {
        const decoded = jwtDecode(accessToken);

        const authData = {
          role: decoded.scope,
          accessToken: accessToken,
          userId: decoded.sub,
          userName: decoded.username,
        };
        Object.entries(authData).forEach(([key, value]) =>
          localStorage.setItem(key, value)
        );

        if (setAuthToken(accessToken, decoded.sub, decoded.username)) {
          // Fetch profile
          try {
            const profileRes = await api.get(`/v1/users/${decoded.username}`);
            const profile = profileRes.data?.body || profileRes.data;
            if (profile?.givenName) localStorage.setItem("givenName", profile.givenName);
            if (profile?.familyName) localStorage.setItem("familyName", profile.familyName);
            if (profile?.profilePictureUrl) localStorage.setItem("profilePictureUrl", profile.profilePictureUrl);
          } catch (_) {}

          window.location.href = "/home";
        }
      } catch {
        window.location.href = "/register?error=google_login_failed";
      }
    };

    handleCallback();
  }, [searchParams]);

  return (
    <div className="min-h-screen bg-background flex items-center justify-center">
      <div className="text-center">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary mx-auto mb-4"></div>
        <p className="text-muted-foreground">Đang xử lý đăng nhập Google...</p>
      </div>
    </div>
  );
}

export default function OAuth2CallbackPage() {
  return (
    <Suspense fallback={
      <div className="min-h-screen bg-background flex items-center justify-center">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-primary"></div>
      </div>
    }>
      <OAuth2CallbackContent />
    </Suspense>
  );
}
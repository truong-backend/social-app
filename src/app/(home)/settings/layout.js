"use client";

import React, { useState, useEffect } from "react";
import Link from "next/link";
import { usePathname, useRouter } from "next/navigation";
import MotionContainer from "@/components/ui-components/MotionContainer";
import {
  UserCircle, Lock, Ban,
  Sun, ArrowLeft, ChevronRight,
} from "lucide-react";
import { usePageMetadata, pageMetadata } from "@/utils/clientMetadata";

const groupedMenuItems = [
  {
    title: "Cài đặt tài khoản",
    items: [
      { id: "personalinfo", icon: UserCircle, label: "Chỉnh sửa trang cá nhân" },
      { id: "privacy", icon: Lock, label: "Quyền riêng tư tài khoản" },
      { id: "blockedlist", icon: Ban, label: "Nội dung bạn đã ẩn" },
    ],
  },
  {
    title: "Cài đặt hiển thị",
    items: [
      { id: "display", icon: Sun, label: "Chế độ tối" },
    ],
  },
];

export default function SettingsLayout({ children }) {
  const pathname = usePathname();
  const router = useRouter();
  const [isMobile, setIsMobile] = useState(false);
  const [showSidebar, setShowSidebar] = useState(true);

  usePageMetadata(pageMetadata.settings());

  useEffect(() => {
    const check = () => setIsMobile(window.innerWidth < 768);
    check();
    window.addEventListener("resize", check);
    return () => window.removeEventListener("resize", check);
  }, []);

  useEffect(() => {
    if (isMobile) {
      const isSubpage = pathname !== "/settings" && pathname.startsWith("/settings/");
      setShowSidebar(!isSubpage);
    } else {
      setShowSidebar(true);
    }
  }, [pathname, isMobile]);

  const getCurrentPageTitle = () => {
    for (const group of groupedMenuItems) {
      for (const item of group.items) {
        if (pathname.endsWith(item.id)) return item.label;
      }
    }
    return "Cài đặt";
  };

  return (
    <div className="flex min-h-screen w-full bg-[var(--background)] text-[var(--foreground)]">
      {/* Sidebar */}
      <aside
        className={`
          ${isMobile ? (showSidebar ? "w-full" : "hidden") : "w-[320px] min-w-[320px]"}
          border-r border-[var(--border)] overflow-y-auto
          ${isMobile ? "fixed inset-0 z-10 bg-[var(--background)]" : ""}
        `}
      >
        {/* Sidebar header */}
        <div className="px-6 py-5 border-b border-[var(--border)]">
          <h2 className="text-xl font-bold tracking-tight">Cài đặt</h2>
        </div>

        <nav className="py-2">
          {groupedMenuItems.map((group, idx) => (
            <div key={idx}>
              <p className="px-6 pt-5 pb-2 text-[11px] font-semibold text-[var(--muted-foreground)] uppercase tracking-widest">
                {group.title}
              </p>
              {group.items.map((item) => {
                const isActive = pathname.endsWith(item.id);
                return (
                  <Link
                    key={item.id}
                    href={`/settings/${item.id}`}
                    onClick={() => isMobile && setShowSidebar(false)}
                    className={`
                      flex items-center justify-between px-6 py-3 
                      hover:bg-[var(--muted)] transition-colors
                      ${isActive ? "bg-[var(--muted)]" : ""}
                    `}
                  >
                    <div className="flex items-center gap-3">
                      <item.icon
                        className={`w-5 h-5 ${isActive ? "text-[var(--foreground)]" : "text-[var(--muted-foreground)]"}`}
                      />
                      <span className={`text-sm ${isActive ? "font-semibold" : "font-normal"}`}>
                        {item.label}
                      </span>
                    </div>
                    {isMobile && (
                      <ChevronRight className="w-4 h-4 text-[var(--muted-foreground)]" />
                    )}
                  </Link>
                );
              })}
            </div>
          ))}

          <div className="px-6 pt-8 pb-4">
            <p className="text-xs text-[var(--muted-foreground)]">Phiên bản 1.0.0</p>
          </div>
        </nav>
      </aside>

      {/* Main content */}
      <main
        className={`
          flex-1 overflow-y-auto
          ${isMobile ? (showSidebar ? "hidden" : "block") : "block"}
        `}
      >
        {/* Mobile back header */}
        {isMobile && !showSidebar && (
          <div className="flex items-center gap-3 px-4 py-4 border-b border-[var(--border)] sticky top-0 bg-[var(--background)] z-10">
            <button
              onClick={() => router.push("/settings")}
              className="p-1 -ml-1 rounded-full hover:bg-[var(--muted)] transition-colors"
            >
              <ArrowLeft className="w-5 h-5" />
            </button>
            <h1 className="text-base font-semibold">{getCurrentPageTitle()}</h1>
          </div>
        )}

        <MotionContainer modeKey={pathname} effect="fadeUp" duration={0.25}>
          {children}
        </MotionContainer>
      </main>
    </div>
  );
}
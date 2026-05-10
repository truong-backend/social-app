"use client";

import Link from "next/link";
import { useState } from "react";
import { PlusSquare } from "lucide-react";
import { useRouter } from "next/navigation";
import api, { clearSession } from "@/utils/axios";
import NewPostModal from "../social-app-component/CreatePostForm";
import useAppStore from "@/store/ZustandStore";

export default function Header() {
  const router = useRouter();
  const [showPostModal, setShowPostModal] = useState(false);
  const [isLoggingOut, setIsLoggingOut] = useState(false);
  const clearAllData = useAppStore((state) => state.clearAllData);

  return (
    <>
      <header
        className="w-full flex items-center justify-between bg-[var(--background)] border-b border-[var(--border)] px-4 md:px-6"
        style={{ height: "60px" }}
      >
        {/* Logo - Instagram style */}
        <div className="flex items-center">
          <Link
            href="/home"
            className="font-bold text-xl tracking-tight text-[var(--foreground)] font-serif italic"
            style={{ fontFamily: "'Billabong', 'Grand Hotel', cursive, Arial" }}
          >
            pocpoc
          </Link>
        </div>

        {/* Right actions */}
        <div className="flex items-center gap-1">
          <button
            type="button"
            aria-label="Tạo bài viết"
            title="Tạo bài viết"
            onClick={() => setShowPostModal(true)}
            disabled={isLoggingOut}
            className="flex items-center gap-2 px-3 py-2 rounded-xl text-sm font-medium text-[var(--foreground)] hover:bg-gray-100 dark:hover:bg-[#1a1a1a] transition-colors disabled:opacity-50"
          >
            <PlusSquare size={22} strokeWidth={1.5} />
            <span className="hidden sm:inline">Đăng bài</span>
          </button>
        </div>
      </header>

      {showPostModal && !isLoggingOut && (
        <NewPostModal
          isOpen={showPostModal}
          onClose={() => setShowPostModal(false)}
        />
      )}
    </>
  );
}
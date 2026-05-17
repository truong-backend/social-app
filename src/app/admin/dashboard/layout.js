"use client"

import { useState } from "react"
import { useRouter, usePathname } from "next/navigation"
import Link from "next/link"
import { LogOut } from "lucide-react"
import adminApi, { clearAdminSession } from "@/utils/adminInterception"
import ConfirmDialog from "@/components/social-app-component/ConfirmDialog"

export default function AdminLayout({ children }) {
  const router = useRouter()
  const pathname = usePathname()
  const [showLogoutConfirm, setShowLogoutConfirm] = useState(false)

  const doLogout = async () => {
    try {
      await adminApi.delete("/v1/auth/logout")
    } catch (err) {
      console.error("Logout failed:", err.response?.data || err.message)
    } finally {
      clearAdminSession()
      localStorage.removeItem("admin_role")
      router.push("/admin/login")
    }
  }

  const getPageInfo = () => {
    if (pathname.includes("/users")) {
      return {
        title: "👥 Users Analytics",
        subtitle: "Thống kê người dùng hệ thống",
        otherPage: { href: "/admin/dashboard/posts", label: "📊 Posts Analytics" },
      }
    } else if (pathname.includes("/posts")) {
      return {
        title: "📊 Posts Analytics",
        subtitle: "Thống kê bài viết hệ thống",
        otherPage: { href: "/admin/dashboard/users", label: "👥 Users Analytics" },
      }
    }
    return {
      title: "📊 Analytics Dashboard",
      subtitle: "Tổng quan thống kê hệ thống",
      otherPage: null,
    }
  }

  const pageInfo = getPageInfo()

  return (
    <div className="min-h-screen" style={{ backgroundColor: "var(--background)", color: "var(--foreground)" }}>
      <ConfirmDialog
        isOpen={showLogoutConfirm}
        title="Đăng xuất?"
        message="Bạn có chắc muốn đăng xuất khỏi trang quản trị không?"
        confirmText="Đăng xuất"
        cancelText="Hủy"
        confirmStyle="danger"
        onConfirm={() => { setShowLogoutConfirm(false); doLogout(); }}
        onCancel={() => setShowLogoutConfirm(false)}
      />
      {/* Header */}
      <div className="shadow-sm" style={{ backgroundColor: "var(--card)", borderBottom: "1px solid var(--border)" }}>
        <div className="max-w-7xl mx-auto px-6 py-4">
          <div className="flex items-center justify-between">
            <div>
              <h1 className="text-3xl font-bold" style={{ color: "var(--foreground)" }}>
                {pageInfo.title}
              </h1>
              <p className="mt-1" style={{ color: "var(--muted-foreground)" }}>
                {pageInfo.subtitle}
              </p>
            </div>
            <div className="flex items-center space-x-4">
              {pageInfo.otherPage && (
                <Link
                  href={pageInfo.otherPage.href}
                  className="flex items-center px-4 py-2 rounded-lg font-medium transition-all duration-200 hover:shadow-md"
                  style={{
                    backgroundColor: "var(--secondary)",
                    color: "var(--secondary-foreground)",
                    border: "1px solid var(--border)",
                  }}
                >
                  {pageInfo.otherPage.label}
                </Link>
              )}
              <button
                onClick={() => setShowLogoutConfirm(true)}
                className="flex items-center px-4 py-2 rounded-lg font-medium transition-all duration-200 hover:shadow-md"
                style={{
                  backgroundColor: "var(--primary)",
                  color: "var(--primary-foreground)",
                  border: "1px solid var(--border)",
                }}
              >
                <LogOut className="w-4 h-4 mr-2" />
                Logout
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Page Content */}
      <div className="max-w-7xl mx-auto px-6 py-8">{children}</div>
    </div>
  )
}
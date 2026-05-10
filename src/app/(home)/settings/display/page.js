"use client";

import React from "react";
import ThemeToggle from "@/components/ui-components/Themetoggle";

function SettingRow({ label, description, action }) {
  return (
    <div className="flex items-center justify-between py-4 border-b border-[var(--border)] last:border-0">
      <div className="flex-1 pr-4">
        <p className="text-sm font-medium text-[var(--foreground)]">{label}</p>
        {description && (
          <p className="text-xs text-[var(--muted-foreground)] mt-0.5">{description}</p>
        )}
      </div>
      <div className="shrink-0">{action}</div>
    </div>
  );
}

export default function DisplaySettings() {
  return (
    <div className="w-full max-w-2xl px-6 sm:px-10 py-8">
      {/* Header */}
      <div className="mb-8 hidden md:block">
        <h1 className="text-xl font-bold">Chế độ tối</h1>
      </div>

      <div className="">
        <SettingRow
          label="Chế độ tối"
          description="Thay đổi giao diện sang chế độ tối"
          action={<ThemeToggle />}
        />
        <SettingRow
          label="Cỡ chữ giao diện"
          description="Tính năng đang phát triển"
          action={
            <select
              disabled
              className="text-xs bg-[var(--muted)] text-[var(--muted-foreground)] px-3 py-1.5 rounded-lg cursor-not-allowed"
              defaultValue="medium"
            >
              <option value="small">Nhỏ</option>
              <option value="medium">Trung bình</option>
              <option value="large">Lớn</option>
            </select>
          }
        />
      </div>
    </div>
  );
}
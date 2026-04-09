package com.socialapp.domain.admin.valueobject;

import java.util.List;

public record Statistics(
        List<StatEntry> commonStats,
        List<StatEntry> weekStats,
        List<StatEntry> monthStats,
        List<StatEntry> yearStats,
        List<StatEntry> allTimeStats
) {}
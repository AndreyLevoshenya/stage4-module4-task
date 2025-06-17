package com.mjc.school.model;

import java.util.List;

public record SearchParameters(
        String newsTitle,
        String newsContent,
        String authorName,
        List<Integer> tagIds,
        List<String> tagNames) {
}

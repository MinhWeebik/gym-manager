package com.ringme.cms.utils;

import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Field;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class AppUtils {
    public static Optional<String> goBack(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader("Referer")).map(requestUrl -> "redirect:" + requestUrl);
    }

    public static Optional<String> goBackWithError(HttpServletRequest request, RedirectAttributes redirectAttributes, String formName, BindingResult bindingResult, Object form, String msgErr) {
        if (msgErr == null) msgErr = "Validation error! Please try again.";
        redirectAttributes.addFlashAttribute(formName, form);
        redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult." + formName, bindingResult);
        redirectAttributes.addFlashAttribute("error", msgErr);
        return Optional.ofNullable(request.getHeader("Referer")).map(requestUrl -> "redirect:" + requestUrl);
    }

    public static Optional<Object> getFieldValue(Object object, String fieldName) {
        try {
            Field field = object.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return Optional.ofNullable(field.get(object));
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return Optional.empty();
        }
    }

    public static boolean containsVietnameseCharacters(String input) {
        // Regular expression pattern to match Vietnamese characters
        String vietnamesePattern = "[\\p{InCombiningDiacriticalMarks}àáảãạâầấẩẫậăằắẳẵặèéẻẽẹêềếểễệìíỉĩịòóỏõọôồốổỗộơờớởỡợùúủũụưừứửữựỳýỷỹỵđĐ]";
        Pattern pattern = Pattern.compile(vietnamesePattern);
        return pattern.matcher(input).find();
    }

    public static String removeVietnameseAccents(String input) {
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }

    public static String removeSpecialCharacters(String input) {
        return input.replaceAll("[^a-zA-Z0-9\\s]", "").toLowerCase();
    }

    public static String processTextToAZ09(String input) {
        String noAccents = removeVietnameseAccents(input);
        return removeSpecialCharacters(noAccents);
    }

    public static String extractPath(String url) {
        String path = null;
        if (url.contains("/ringbacktones")) {
            String[] parts = url.split("/ringbacktones", 2);
            if (parts.length > 1) {
                path = "ringbacktones" + parts[1];
            }
        } else if (url.contains("/images")) {
            String[] parts = url.split("/images", 2);
            if (parts.length > 1) {
                path = "images" + parts[1];
            }
        } else if (url.contains("/singer")) {
            String[] parts = url.split("/singer", 2);
            if (parts.length > 1) {
                path = "singer" + parts[1];
            }
        }
        return path;
    }
}

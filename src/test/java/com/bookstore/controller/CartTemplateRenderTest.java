package com.bookstore.controller;

import com.bookstore.dto.CartDto;
import com.bookstore.dto.CartItemDto;
import com.bookstore.entity.Branch;
import com.bookstore.entity.Category;
import com.bookstore.entity.Coupon;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CartTemplateRenderTest {

    @Test
    public void testRenderCartTemplate() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);

        MockServletContext servletContext = new MockServletContext();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();
        IWebExchange exchange = application.buildExchange(request, response);

        WebContext context = new WebContext(exchange);

        CartDto cart = new CartDto();
        cart.setShippingFee(new BigDecimal("25000"));
        cart.setDiscountAmount(BigDecimal.ZERO);
        cart.setShippingDiscountAmount(BigDecimal.ZERO);

        CartItemDto item = new CartItemDto();
        item.setBookId(1L);
        item.setTitle("Chí Phèo");
        item.setAuthorName("Nam Cao");
        item.setPrice(new BigDecimal("65000"));
        item.setQuantity(1);
        item.setConditionGrade("LIKE_NEW");
        item.setBranchName("Book4Life Quận 1");
        cart.getItems().add(item);

        context.setVariable("cart", cart);

        List<Branch> branches = new ArrayList<>();
        Branch b = new Branch();
        b.setId(1L);
        b.setBranchName("Book4Life Quận 1");
        b.setAddress("123 Nguyễn Thị Minh Khai");
        branches.add(b);
        context.setVariable("branches", branches);

        List<Category> categories = new ArrayList<>();
        Category c = new Category();
        c.setId(1L);
        c.setName("Kinh Điển");
        categories.add(c);
        context.setVariable("categories", categories);

        List<Coupon> coupons = new ArrayList<>();
        Coupon cp = new Coupon();
        cp.setCode("FREESHIP");
        cp.setDiscountType("FREESHIP");
        cp.setCouponCategory("SHIPPING");
        coupons.add(cp);

        Coupon cp2 = new Coupon();
        cp2.setCode("BOOK10");
        cp2.setDiscountType("PERCENT");
        cp2.setDiscountValue(new BigDecimal("10"));
        cp2.setCouponCategory("ORDER_DISCOUNT");
        coupons.add(cp2);

        context.setVariable("availableCoupons", coupons);
        context.setVariable("pageTitle", "Giỏ Hàng");

        String rendered = engine.process("cart", context);
        assertNotNull(rendered);
        System.out.println("RENDER SUCCESS! Length: " + rendered.length());
        System.out.println("Contains checkoutModal: " + rendered.contains("id=\"checkoutModal\""));
        System.out.println("Contains openCheckoutModal: " + rendered.contains("openCheckoutModal"));
        System.out.println("Contains footer: " + rendered.contains("Book4Life Literary Warmth Palette"));
        assertTrue(rendered.contains("id=\"checkoutModal\""));
        assertTrue(rendered.contains("openCheckoutModal"));
    }
}


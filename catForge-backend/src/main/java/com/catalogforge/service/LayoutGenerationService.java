package com.catalogforge.service;

import com.catalogforge.agent.AgentContext;
import com.catalogforge.agent.AgentOrchestrator;
import com.catalogforge.exception.LayoutGenerationException;
import com.catalogforge.exception.ResourceNotFoundException;
import com.catalogforge.model.Layout;
import com.catalogforge.model.LayoutVariant;
import com.catalogforge.model.Product;
import com.catalogforge.model.request.LayoutOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for layout generation and lifecycle management.
 */
@Service
public class LayoutGenerationService {

    private static final Logger log = LoggerFactory.getLogger(LayoutGenerationService.class);

    private final AgentOrchestrator orchestrator;
    private final ProductService productService;
    private final Map<String, Layout> layoutStore = new ConcurrentHashMap<>();

    public LayoutGenerationService(AgentOrchestrator orchestrator, ProductService productService) {
        this.orchestrator = orchestrator;
        this.productService = productService;
    }

    /**
     * Generates a layout from text prompt.
     */
    public Layout generateFromText(List<Long> productIds, LayoutOptions options, String userPrompt) {
        log.info("Generating layout from text: products={}, options={}", productIds, options);
        
        List<Product> products = resolveProducts(productIds);
        
        AgentContext context = AgentContext.forTextGeneration(products, options, userPrompt);
        AgentContext result = orchestrator.execute(context);
        
        if (result.generatedLayout() == null) {
            throw new LayoutGenerationException("Layout generation failed - no layout produced");
        }
        
        Layout layout = result.generatedLayout();
        layoutStore.put(layout.id(), layout);
        
        log.info("Layout generated: id={}, variants={}", layout.id(), layout.variants().size());
        return layout;
    }

    /**
     * Generates a layout from image reference.
     */
    public Layout generateFromImage(
            List<Long> productIds, 
            LayoutOptions options, 
            String userPrompt,
            String imageBase64,
            String imageMimeType
    ) {
        log.info("Generating layout from image: products={}, mimeType={}", productIds, imageMimeType);
        
        List<Product> products = resolveProducts(productIds);
        
        AgentContext context = AgentContext.forImageGeneration(
                products, options, userPrompt, imageBase64, imageMimeType
        );
        AgentContext result = orchestrator.execute(context);
        
        if (result.generatedLayout() == null) {
            throw new LayoutGenerationException("Layout generation failed - no layout produced");
        }
        
        Layout layout = result.generatedLayout();
        layoutStore.put(layout.id(), layout);
        
        log.info("Layout generated from image: id={}, variants={}", 
                layout.id(), layout.variants().size());
        return layout;
    }

    /**
     * Retrieves a layout by ID.
     */
    public Layout getLayout(String layoutId) {
        return Optional.ofNullable(layoutStore.get(layoutId))
                .orElseThrow(() -> new ResourceNotFoundException("Layout", layoutId));
    }

    /**
     * Retrieves a layout by ID, returning Optional.
     */
    public Optional<Layout> findLayout(String layoutId) {
        return Optional.ofNullable(layoutStore.get(layoutId));
    }

    /**
     * Updates a layout.
     */
    public Layout updateLayout(String layoutId, Layout updatedLayout) {
        if (!layoutStore.containsKey(layoutId)) {
            throw new ResourceNotFoundException("Layout", layoutId);
        }
        
        layoutStore.put(layoutId, updatedLayout);
        log.info("Layout updated: id={}", layoutId);
        return updatedLayout;
    }

    /**
     * Deletes a layout.
     */
    public void deleteLayout(String layoutId) {
        if (layoutStore.remove(layoutId) == null) {
            throw new ResourceNotFoundException("Layout", layoutId);
        }
        log.info("Layout deleted: id={}", layoutId);
    }

    /**
     * Gets all variants for a layout.
     */
    public List<LayoutVariant> getVariants(String layoutId) {
        Layout layout = getLayout(layoutId);
        return layout.variants();
    }

    /**
     * Gets a specific variant.
     */
    public LayoutVariant getVariant(String layoutId, String variantId) {
        Layout layout = getLayout(layoutId);
        return layout.variants().stream()
                .filter(v -> v.id().equals(variantId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Variant", variantId));
    }

    /**
     * Returns the number of stored layouts.
     */
    public int getLayoutCount() {
        return layoutStore.size();
    }

    private List<Product> resolveProducts(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        
        return productIds.stream()
                .map(id -> productService.findProductById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product", id.toString())))
                .toList();
    }
}

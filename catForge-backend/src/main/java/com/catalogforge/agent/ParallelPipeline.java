package com.catalogforge.agent;

import com.catalogforge.agent.steps.LayoutGenerationStep;
import com.catalogforge.agent.steps.PromptAssemblyStep;
import com.catalogforge.agent.steps.ValidationStep;
import com.catalogforge.model.Layout;
import com.catalogforge.model.LayoutVariant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Parallel pipeline for generating multiple layout variants simultaneously.
 * Executes N parallel layout generations and aggregates results.
 */
@Component
public class ParallelPipeline implements Pipeline {

    private static final Logger log = LoggerFactory.getLogger(ParallelPipeline.class);
    private static final int DEFAULT_VARIANT_COUNT = 3;

    private final PromptAssemblyStep promptAssemblyStep;
    private final LayoutGenerationStep layoutGenerationStep;
    private final ValidationStep validationStep;
    private final ExecutorService executor;

    public ParallelPipeline(
            PromptAssemblyStep promptAssemblyStep,
            LayoutGenerationStep layoutGenerationStep,
            ValidationStep validationStep
    ) {
        this.promptAssemblyStep = promptAssemblyStep;
        this.layoutGenerationStep = layoutGenerationStep;
        this.validationStep = validationStep;
        this.executor = Executors.newFixedThreadPool(4);
    }

    @Override
    public AgentContext run(AgentContext context) {
        log.info("Starting ParallelPipeline: {}", context.pipelineId());
        
        int variantCount = context.options() != null 
                ? context.options().variantCount() 
                : DEFAULT_VARIANT_COUNT;
        
        // Step 1: Assemble base prompt
        AgentContext baseContext = promptAssemblyStep.execute(context);
        
        // Step 2: Generate variants in parallel
        List<CompletableFuture<AgentContext>> futures = new ArrayList<>();
        
        for (int i = 0; i < variantCount; i++) {
            final int variantIndex = i;
            CompletableFuture<AgentContext> future = CompletableFuture.supplyAsync(() -> {
                log.debug("Generating variant {} of {}", variantIndex + 1, variantCount);
                
                // Add variant-specific instructions
                AgentContext variantContext = baseContext.withAssembledPrompt(
                        baseContext.assembledPrompt() + "\n\n" + 
                        getVariantInstructions(variantIndex, variantCount)
                );
                
                // Generate and validate
                AgentContext generated = layoutGenerationStep.execute(variantContext);
                return validationStep.execute(generated);
                
            }, executor);
            
            futures.add(future);
        }
        
        // Step 3: Collect results
        List<AgentContext> results = futures.stream()
                .map(CompletableFuture::join)
                .toList();
        
        // Step 4: Aggregate variants
        List<LayoutVariant> allVariants = results.stream()
                .filter(ctx -> ctx.generatedLayout() != null)
                .flatMap(ctx -> ctx.generatedLayout().variants().stream())
                .collect(Collectors.toList());
        
        // Use first valid layout as base, add all variants
        AgentContext firstValid = results.stream()
                .filter(AgentContext::isValid)
                .findFirst()
                .orElse(results.get(0));
        
        if (firstValid.generatedLayout() != null) {
            Layout aggregatedLayout = firstValid.generatedLayout().withVariants(allVariants);
            firstValid = firstValid.withGeneratedLayout(aggregatedLayout);
        }
        
        log.info("ParallelPipeline completed: {} variants generated", allVariants.size());
        return firstValid.withVariants(allVariants);
    }

    private String getVariantInstructions(int index, int total) {
        String[] styles = {
                "Create a clean, minimalist design with ample whitespace",
                "Create a bold, dynamic design with strong visual hierarchy",
                "Create an elegant, premium design with refined typography",
                "Create a modern, tech-focused design with geometric elements",
                "Create a warm, approachable design with rounded elements"
        };
        
        String style = styles[index % styles.length];
        
        return String.format("""
                ## Variant Instructions (Variant %d of %d)
                
                %s
                
                Make this variant distinct from others while maintaining brand consistency.
                """, index + 1, total, style);
    }

    @Override
    public String name() {
        return "ParallelPipeline";
    }
}

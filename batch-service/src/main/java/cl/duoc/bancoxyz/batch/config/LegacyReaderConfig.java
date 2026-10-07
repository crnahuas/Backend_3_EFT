package cl.duoc.bancoxyz.batch.config;

import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.file.FlatFileItemReader;
import org.springframework.batch.infrastructure.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;

import cl.duoc.bancoxyz.batch.domain.EstadoFinancieroAnualInput;
import cl.duoc.bancoxyz.batch.domain.InteresMensualInput;
import cl.duoc.bancoxyz.batch.domain.MovimientoDiarioInput;

@Configuration
public class LegacyReaderConfig {

    @Bean
    @StepScope
    FlatFileItemReader<MovimientoDiarioInput> movimientoDiarioReader(
            @Value("#{stepExecutionContext['file']}") Object resourceValue) {
        Resource resource = resolve(resourceValue);
        String sourceFile = resource.getDescription();
        return new FlatFileItemReaderBuilder<MovimientoDiarioInput>()
                .name("movimientoDiarioReader")
                .resource(resource)
                .encoding("UTF-8")
                .linesToSkip(1)
                .delimited(delimited -> delimited
                        .delimiter(",")
                        .names("id", "fecha", "monto", "tipo"))
                .fieldSetMapper(fields -> new MovimientoDiarioInput(
                        sourceFile,
                        fields.readLong("id"),
                        fields.readString("fecha"),
                        fields.readString("monto"),
                        fields.readString("tipo")))
                .build();
    }

    @Bean
    @StepScope
    FlatFileItemReader<InteresMensualInput> interesMensualReader(
            @Value("#{stepExecutionContext['file']}") Object resourceValue) {
        Resource resource = resolve(resourceValue);
        String sourceFile = resource.getDescription();
        return new FlatFileItemReaderBuilder<InteresMensualInput>()
                .name("interesMensualReader")
                .resource(resource)
                .encoding("UTF-8")
                .linesToSkip(1)
                .delimited(delimited -> delimited
                        .delimiter(",")
                        .names("cuenta_id", "nombre", "saldo", "edad", "tipo"))
                .fieldSetMapper(fields -> new InteresMensualInput(
                        sourceFile,
                        fields.readLong("cuenta_id"),
                        fields.readString("nombre"),
                        fields.readString("saldo"),
                        fields.readString("edad"),
                        fields.readString("tipo")))
                .build();
    }

    @Bean
    @StepScope
    FlatFileItemReader<EstadoFinancieroAnualInput> estadoFinancieroAnualReader(
            @Value("#{stepExecutionContext['file']}") Object resourceValue) {
        Resource resource = resolve(resourceValue);
        String sourceFile = resource.getDescription();
        return new FlatFileItemReaderBuilder<EstadoFinancieroAnualInput>()
                .name("estadoFinancieroAnualReader")
                .resource(resource)
                .encoding("UTF-8")
                .linesToSkip(1)
                .delimited(delimited -> delimited
                        .delimiter(",")
                        .names("cuenta_id", "fecha", "transaccion", "monto", "descripcion"))
                .fieldSetMapper(fields -> new EstadoFinancieroAnualInput(
                        sourceFile,
                        fields.readLong("cuenta_id"),
                        fields.readString("fecha"),
                        fields.readString("transaccion"),
                        fields.readString("monto"),
                        fields.readString("descripcion")))
                .build();
    }

    private Resource resolve(Object value) {
        if (value instanceof Resource resource) {
            return resource;
        }
        if (value instanceof String location) {
            return new DefaultResourceLoader().getResource(location);
        }
        throw new IllegalArgumentException("La particion no contiene el recurso legacy esperado");
    }
}

// Copyright (c) Philipp Wagner. All rights reserved.
// Licensed under the MIT license. See LICENSE file in the project root for full license information.

package elastic.mapping;

import elastic.exceptions.GetMappingFailedException;
import org.elasticsearch.Version;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.json.JsonXContent;
import org.elasticsearch.index.mapper.ContentPath;
import org.elasticsearch.index.mapper.Mapper;
import org.elasticsearch.index.mapper.Mapping;
import org.elasticsearch.index.mapper.MetadataFieldMapper;
import org.elasticsearch.index.mapper.object.RootObjectMapper;

import java.io.IOException;

public abstract class AbstractMap implements IObjectMapping {

    private final String indexType;

    public AbstractMap(String indexType) {
        this.indexType = indexType;
    }

    public XContentBuilder getMapping() {
        try {
            return internalGetMapping();
        } catch(Exception e) {
            throw new GetMappingFailedException(indexType, e);
        }
    }

    public String getIndexType() {
        return indexType;
    }

    public XContentBuilder internalGetMapping() throws IOException {

        RootObjectMapper.Builder rootObjectMapperBuilder = getRootObjectBuilder();
        Settings.Builder settingsBuilder = getSettingsBuilder();

        Mapping mapping = new Mapping(
                Version.fromString("1.0.0"),
                rootObjectMapperBuilder.build(new Mapper.BuilderContext(settingsBuilder.build(), new ContentPath())),
                new MetadataFieldMapper[] {},
                new Mapping.SourceTransform[] {},
                null);

        return mapping.toXContent(JsonXContent.contentBuilder().startObject(), ToXContent.EMPTY_PARAMS);
    }

    private Settings.Builder getSettingsBuilder() {
        Settings.Builder settingsBuilder = Settings.builder();

        configure(settingsBuilder);

        return settingsBuilder;
    }

    private RootObjectMapper.Builder getRootObjectBuilder() {
        RootObjectMapper.Builder rootObjectMapperBuilder = new RootObjectMapper.Builder(indexType);

        configure(rootObjectMapperBuilder);

        return rootObjectMapperBuilder;
    }

    protected abstract void configure(RootObjectMapper.Builder builder);

    protected abstract void configure(Settings.Builder builder);
}
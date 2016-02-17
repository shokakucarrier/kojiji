package com.redhat.red.build.koji.model.json;

import static com.redhat.red.build.koji.model.json.KojiJsonConstants.*;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by jdcasey on 2/10/16.
 */
public class ImportInfo
{
    @JsonProperty(METADATA_VERSION)
    private int metadataVersion;

    @JsonProperty(BUILD)
    private BuildDescription build;

    @JsonProperty( BUILDROOTS )
    private Set<BuildRoot> buildRoots;

    @JsonProperty( OUTPUT )
    private Set<BuildOutput> outputs;

    public ImportInfo( @JsonProperty( METADATA_VERSION ) int metadataVersion,
                       @JsonProperty( BUILD ) BuildDescription build,
                       @JsonProperty( BUILDROOTS ) Set<BuildRoot> buildRoots,
                       @JsonProperty( OUTPUT ) Set<BuildOutput> outputs )
    {
        this.metadataVersion = metadataVersion;
        this.build = build;
        this.buildRoots = buildRoots;
        this.outputs = outputs;
    }

    public int getMetadataVersion()
    {
        return metadataVersion;
    }

    public BuildDescription getBuild()
    {
        return build;
    }

    public Set<BuildRoot> getBuildRoots()
    {
        return buildRoots;
    }

    public Set<BuildOutput> getOutputs()
    {
        return outputs;
    }

    public static final class Builder
            implements SectionBuilder<ImportInfo>
    {
        private int metadataVersion = DEFAULT_METADATA_VERSION;

        private BuildDescription.Builder descBuilder;

        private Set<BuildRoot.Builder> rootBuilders = new HashSet<>();

        private Set<BuildOutput.Builder> outputBuilders = new HashSet<>();

        public Builder()
        {
        }

        public ImportInfo build()
                throws VerificationException
        {
            Set<String> missing = new HashSet<>();
            if ( descBuilder == null )
            {
                missing.add( BUILD );
            }
            else
            {
                descBuilder.findMissingProperties( BUILD + ".%s", missing );
            }

            if ( rootBuilders.isEmpty() )
            {
                missing.add( BUILDROOTS );
            }
            else
            {
                for( BuildRoot.Builder rootBuilder: rootBuilders)
                {
                    rootBuilder.findMissingProperties( BUILDROOTS + "[" + rootBuilder.getId() + "].%s", missing );
                }
            }

            if ( outputBuilders.isEmpty() )
            {
                missing.add( OUTPUT );
            }
            else
            {
                for( BuildOutput.Builder outputBuilder: outputBuilders)
                {
                    outputBuilder.findMissingProperties( OUTPUT + "[" + outputBuilder.getFilename() + "].%s", missing );
                }
            }

            if ( missing.isEmpty() )
            {
                BuildDescription desc = descBuilder.build();
                Set<BuildRoot> buildRoots =
                        rootBuilders.stream().map( ( builder ) -> builder.unsafeBuild() ).collect( Collectors.toSet() );

                Set<BuildOutput> buildOutputs =
                        outputBuilders.stream().map( ( builder ) -> builder.unsafeBuild() ).collect( Collectors.toSet() );

                return new ImportInfo( metadataVersion, desc, buildRoots, buildOutputs );
            }

            throw new VerificationException( missing );
        }

        public Builder withMetadataVersion( int metadataVersion )
        {
            this.metadataVersion = metadataVersion;

            return this;
        }

        public BuildDescription.Builder withNewBuildDescription( String name, String version, String release )
        {
            this.descBuilder = new BuildDescription.Builder( name, version, release, this );

            return descBuilder;
        }

        public BuildRoot.Builder withNewBuildRoot( int id )
        {
            BuildRoot.Builder builder = new BuildRoot.Builder( id, this );
            rootBuilders.add( builder );

            return builder;
        }

        public BuildOutput.Builder withNewOutput( int buildrootId, String filename )
        {
            BuildOutput.Builder builder = new BuildOutput.Builder( buildrootId, filename, this );
            outputBuilders.add( builder );

            return builder;
        }
    }

    @Override
    public boolean equals( Object o )
    {
        if ( this == o )
        {
            return true;
        }
        if ( !( o instanceof ImportInfo ) )
        {
            return false;
        }

        ImportInfo that = (ImportInfo) o;

        if ( getMetadataVersion() != that.getMetadataVersion() )
        {
            return false;
        }
        if ( getBuild() != null ? !getBuild().equals( that.getBuild() ) : that.getBuild() != null )
        {
            return false;
        }
        if ( getBuildRoots() != null ? !getBuildRoots().equals( that.getBuildRoots() ) : that.getBuildRoots() != null )
        {
            return false;
        }
        return !( getOutputs() != null ? !getOutputs().equals( that.getOutputs() ) : that.getOutputs() != null );

    }

    @Override
    public int hashCode()
    {
        int result = getMetadataVersion();
        result = 31 * result + ( getBuild() != null ? getBuild().hashCode() : 0 );
        result = 31 * result + ( getBuildRoots() != null ? getBuildRoots().hashCode() : 0 );
        result = 31 * result + ( getOutputs() != null ? getOutputs().hashCode() : 0 );
        return result;
    }
}

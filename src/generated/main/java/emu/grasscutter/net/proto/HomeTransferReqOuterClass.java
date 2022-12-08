// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: HomeTransferReq.proto

package emu.grasscutter.net.proto;

public final class HomeTransferReqOuterClass {
  private HomeTransferReqOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface HomeTransferReqOrBuilder extends
      // @@protoc_insertion_point(interface_extends:HomeTransferReq)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>uint32 guid = 1;</code>
     * @return The guid.
     */
    int getGuid();

    /**
     * <code>bool Unk3100_KEMFDDMEBIG = 12;</code>
     * @return The unk3100KEMFDDMEBIG.
     */
    boolean getUnk3100KEMFDDMEBIG();
  }
  /**
   * <pre>
   * CmdId: 4726
   * EnetChannelId: 0
   * EnetIsReliable: true
   * IsAllowClient: true
   * </pre>
   *
   * Protobuf type {@code HomeTransferReq}
   */
  public static final class HomeTransferReq extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:HomeTransferReq)
      HomeTransferReqOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use HomeTransferReq.newBuilder() to construct.
    private HomeTransferReq(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private HomeTransferReq() {
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new HomeTransferReq();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private HomeTransferReq(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      this();
      if (extensionRegistry == null) {
        throw new java.lang.NullPointerException();
      }
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder();
      try {
        boolean done = false;
        while (!done) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              done = true;
              break;
            case 8: {

              guid_ = input.readUInt32();
              break;
            }
            case 96: {

              unk3100KEMFDDMEBIG_ = input.readBool();
              break;
            }
            default: {
              if (!parseUnknownField(
                  input, unknownFields, extensionRegistry, tag)) {
                done = true;
              }
              break;
            }
          }
        }
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
        throw e.setUnfinishedMessage(this);
      } catch (com.google.protobuf.UninitializedMessageException e) {
        throw e.asInvalidProtocolBufferException().setUnfinishedMessage(this);
      } catch (java.io.IOException e) {
        throw new com.google.protobuf.InvalidProtocolBufferException(
            e).setUnfinishedMessage(this);
      } finally {
        this.unknownFields = unknownFields.build();
        makeExtensionsImmutable();
      }
    }
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return emu.grasscutter.net.proto.HomeTransferReqOuterClass.internal_static_HomeTransferReq_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return emu.grasscutter.net.proto.HomeTransferReqOuterClass.internal_static_HomeTransferReq_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq.class, emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq.Builder.class);
    }

    public static final int GUID_FIELD_NUMBER = 1;
    private int guid_;
    /**
     * <code>uint32 guid = 1;</code>
     * @return The guid.
     */
    @java.lang.Override
    public int getGuid() {
      return guid_;
    }

    public static final int UNK3100_KEMFDDMEBIG_FIELD_NUMBER = 12;
    private boolean unk3100KEMFDDMEBIG_;
    /**
     * <code>bool Unk3100_KEMFDDMEBIG = 12;</code>
     * @return The unk3100KEMFDDMEBIG.
     */
    @java.lang.Override
    public boolean getUnk3100KEMFDDMEBIG() {
      return unk3100KEMFDDMEBIG_;
    }

    private byte memoizedIsInitialized = -1;
    @java.lang.Override
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized == 1) return true;
      if (isInitialized == 0) return false;

      memoizedIsInitialized = 1;
      return true;
    }

    @java.lang.Override
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      if (guid_ != 0) {
        output.writeUInt32(1, guid_);
      }
      if (unk3100KEMFDDMEBIG_ != false) {
        output.writeBool(12, unk3100KEMFDDMEBIG_);
      }
      unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (guid_ != 0) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt32Size(1, guid_);
      }
      if (unk3100KEMFDDMEBIG_ != false) {
        size += com.google.protobuf.CodedOutputStream
          .computeBoolSize(12, unk3100KEMFDDMEBIG_);
      }
      size += unknownFields.getSerializedSize();
      memoizedSize = size;
      return size;
    }

    @java.lang.Override
    public boolean equals(final java.lang.Object obj) {
      if (obj == this) {
       return true;
      }
      if (!(obj instanceof emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq)) {
        return super.equals(obj);
      }
      emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq other = (emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq) obj;

      if (getGuid()
          != other.getGuid()) return false;
      if (getUnk3100KEMFDDMEBIG()
          != other.getUnk3100KEMFDDMEBIG()) return false;
      if (!unknownFields.equals(other.unknownFields)) return false;
      return true;
    }

    @java.lang.Override
    public int hashCode() {
      if (memoizedHashCode != 0) {
        return memoizedHashCode;
      }
      int hash = 41;
      hash = (19 * hash) + getDescriptor().hashCode();
      hash = (37 * hash) + GUID_FIELD_NUMBER;
      hash = (53 * hash) + getGuid();
      hash = (37 * hash) + UNK3100_KEMFDDMEBIG_FIELD_NUMBER;
      hash = (53 * hash) + com.google.protobuf.Internal.hashBoolean(
          getUnk3100KEMFDDMEBIG());
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }

    @java.lang.Override
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder() {
      return DEFAULT_INSTANCE.toBuilder();
    }
    public static Builder newBuilder(emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq prototype) {
      return DEFAULT_INSTANCE.toBuilder().mergeFrom(prototype);
    }
    @java.lang.Override
    public Builder toBuilder() {
      return this == DEFAULT_INSTANCE
          ? new Builder() : new Builder().mergeFrom(this);
    }

    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    /**
     * <pre>
     * CmdId: 4726
     * EnetChannelId: 0
     * EnetIsReliable: true
     * IsAllowClient: true
     * </pre>
     *
     * Protobuf type {@code HomeTransferReq}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:HomeTransferReq)
        emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReqOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return emu.grasscutter.net.proto.HomeTransferReqOuterClass.internal_static_HomeTransferReq_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return emu.grasscutter.net.proto.HomeTransferReqOuterClass.internal_static_HomeTransferReq_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq.class, emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq.Builder.class);
      }

      // Construct using emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }

      private Builder(
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessageV3
                .alwaysUseFieldBuilders) {
        }
      }
      @java.lang.Override
      public Builder clear() {
        super.clear();
        guid_ = 0;

        unk3100KEMFDDMEBIG_ = false;

        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return emu.grasscutter.net.proto.HomeTransferReqOuterClass.internal_static_HomeTransferReq_descriptor;
      }

      @java.lang.Override
      public emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq getDefaultInstanceForType() {
        return emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq.getDefaultInstance();
      }

      @java.lang.Override
      public emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq build() {
        emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq buildPartial() {
        emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq result = new emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq(this);
        result.guid_ = guid_;
        result.unk3100KEMFDDMEBIG_ = unk3100KEMFDDMEBIG_;
        onBuilt();
        return result;
      }

      @java.lang.Override
      public Builder clone() {
        return super.clone();
      }
      @java.lang.Override
      public Builder setField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.setField(field, value);
      }
      @java.lang.Override
      public Builder clearField(
          com.google.protobuf.Descriptors.FieldDescriptor field) {
        return super.clearField(field);
      }
      @java.lang.Override
      public Builder clearOneof(
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
        return super.clearOneof(oneof);
      }
      @java.lang.Override
      public Builder setRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          int index, java.lang.Object value) {
        return super.setRepeatedField(field, index, value);
      }
      @java.lang.Override
      public Builder addRepeatedField(
          com.google.protobuf.Descriptors.FieldDescriptor field,
          java.lang.Object value) {
        return super.addRepeatedField(field, value);
      }
      @java.lang.Override
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq) {
          return mergeFrom((emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq other) {
        if (other == emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq.getDefaultInstance()) return this;
        if (other.getGuid() != 0) {
          setGuid(other.getGuid());
        }
        if (other.getUnk3100KEMFDDMEBIG() != false) {
          setUnk3100KEMFDDMEBIG(other.getUnk3100KEMFDDMEBIG());
        }
        this.mergeUnknownFields(other.unknownFields);
        onChanged();
        return this;
      }

      @java.lang.Override
      public final boolean isInitialized() {
        return true;
      }

      @java.lang.Override
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private int guid_ ;
      /**
       * <code>uint32 guid = 1;</code>
       * @return The guid.
       */
      @java.lang.Override
      public int getGuid() {
        return guid_;
      }
      /**
       * <code>uint32 guid = 1;</code>
       * @param value The guid to set.
       * @return This builder for chaining.
       */
      public Builder setGuid(int value) {
        
        guid_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>uint32 guid = 1;</code>
       * @return This builder for chaining.
       */
      public Builder clearGuid() {
        
        guid_ = 0;
        onChanged();
        return this;
      }

      private boolean unk3100KEMFDDMEBIG_ ;
      /**
       * <code>bool Unk3100_KEMFDDMEBIG = 12;</code>
       * @return The unk3100KEMFDDMEBIG.
       */
      @java.lang.Override
      public boolean getUnk3100KEMFDDMEBIG() {
        return unk3100KEMFDDMEBIG_;
      }
      /**
       * <code>bool Unk3100_KEMFDDMEBIG = 12;</code>
       * @param value The unk3100KEMFDDMEBIG to set.
       * @return This builder for chaining.
       */
      public Builder setUnk3100KEMFDDMEBIG(boolean value) {
        
        unk3100KEMFDDMEBIG_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>bool Unk3100_KEMFDDMEBIG = 12;</code>
       * @return This builder for chaining.
       */
      public Builder clearUnk3100KEMFDDMEBIG() {
        
        unk3100KEMFDDMEBIG_ = false;
        onChanged();
        return this;
      }
      @java.lang.Override
      public final Builder setUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.setUnknownFields(unknownFields);
      }

      @java.lang.Override
      public final Builder mergeUnknownFields(
          final com.google.protobuf.UnknownFieldSet unknownFields) {
        return super.mergeUnknownFields(unknownFields);
      }


      // @@protoc_insertion_point(builder_scope:HomeTransferReq)
    }

    // @@protoc_insertion_point(class_scope:HomeTransferReq)
    private static final emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq();
    }

    public static emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<HomeTransferReq>
        PARSER = new com.google.protobuf.AbstractParser<HomeTransferReq>() {
      @java.lang.Override
      public HomeTransferReq parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new HomeTransferReq(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<HomeTransferReq> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<HomeTransferReq> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public emu.grasscutter.net.proto.HomeTransferReqOuterClass.HomeTransferReq getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_HomeTransferReq_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_HomeTransferReq_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\025HomeTransferReq.proto\"<\n\017HomeTransferR" +
      "eq\022\014\n\004guid\030\001 \001(\r\022\033\n\023Unk3100_KEMFDDMEBIG\030" +
      "\014 \001(\010B\026\n\024emu.grasscutter.net.protob\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        });
    internal_static_HomeTransferReq_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_HomeTransferReq_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_HomeTransferReq_descriptor,
        new java.lang.String[] { "Guid", "Unk3100KEMFDDMEBIG", });
  }

  // @@protoc_insertion_point(outer_class_scope)
}

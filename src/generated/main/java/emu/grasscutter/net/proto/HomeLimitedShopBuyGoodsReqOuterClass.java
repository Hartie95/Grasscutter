// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: HomeLimitedShopBuyGoodsReq.proto

package emu.grasscutter.net.proto;

public final class HomeLimitedShopBuyGoodsReqOuterClass {
  private HomeLimitedShopBuyGoodsReqOuterClass() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistryLite registry) {
  }

  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
    registerAllExtensions(
        (com.google.protobuf.ExtensionRegistryLite) registry);
  }
  public interface HomeLimitedShopBuyGoodsReqOrBuilder extends
      // @@protoc_insertion_point(interface_extends:HomeLimitedShopBuyGoodsReq)
      com.google.protobuf.MessageOrBuilder {

    /**
     * <code>.HomeLimitedShopGoods goods = 3;</code>
     * @return Whether the goods field is set.
     */
    boolean hasGoods();
    /**
     * <code>.HomeLimitedShopGoods goods = 3;</code>
     * @return The goods.
     */
    emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods getGoods();
    /**
     * <code>.HomeLimitedShopGoods goods = 3;</code>
     */
    emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoodsOrBuilder getGoodsOrBuilder();

    /**
     * <code>uint32 buy_count = 10;</code>
     * @return The buyCount.
     */
    int getBuyCount();
  }
  /**
   * <pre>
   * CmdId: 4760
   * EnetChannelId: 0
   * EnetIsReliable: true
   * IsAllowClient: true
   * </pre>
   *
   * Protobuf type {@code HomeLimitedShopBuyGoodsReq}
   */
  public static final class HomeLimitedShopBuyGoodsReq extends
      com.google.protobuf.GeneratedMessageV3 implements
      // @@protoc_insertion_point(message_implements:HomeLimitedShopBuyGoodsReq)
      HomeLimitedShopBuyGoodsReqOrBuilder {
  private static final long serialVersionUID = 0L;
    // Use HomeLimitedShopBuyGoodsReq.newBuilder() to construct.
    private HomeLimitedShopBuyGoodsReq(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
      super(builder);
    }
    private HomeLimitedShopBuyGoodsReq() {
    }

    @java.lang.Override
    @SuppressWarnings({"unused"})
    protected java.lang.Object newInstance(
        UnusedPrivateParameter unused) {
      return new HomeLimitedShopBuyGoodsReq();
    }

    @java.lang.Override
    public final com.google.protobuf.UnknownFieldSet
    getUnknownFields() {
      return this.unknownFields;
    }
    private HomeLimitedShopBuyGoodsReq(
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
            case 26: {
              emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods.Builder subBuilder = null;
              if (goods_ != null) {
                subBuilder = goods_.toBuilder();
              }
              goods_ = input.readMessage(emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods.parser(), extensionRegistry);
              if (subBuilder != null) {
                subBuilder.mergeFrom(goods_);
                goods_ = subBuilder.buildPartial();
              }

              break;
            }
            case 80: {

              buyCount_ = input.readUInt32();
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
      return emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.internal_static_HomeLimitedShopBuyGoodsReq_descriptor;
    }

    @java.lang.Override
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.internal_static_HomeLimitedShopBuyGoodsReq_fieldAccessorTable
          .ensureFieldAccessorsInitialized(
              emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq.class, emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq.Builder.class);
    }

    public static final int GOODS_FIELD_NUMBER = 3;
    private emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods goods_;
    /**
     * <code>.HomeLimitedShopGoods goods = 3;</code>
     * @return Whether the goods field is set.
     */
    @java.lang.Override
    public boolean hasGoods() {
      return goods_ != null;
    }
    /**
     * <code>.HomeLimitedShopGoods goods = 3;</code>
     * @return The goods.
     */
    @java.lang.Override
    public emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods getGoods() {
      return goods_ == null ? emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods.getDefaultInstance() : goods_;
    }
    /**
     * <code>.HomeLimitedShopGoods goods = 3;</code>
     */
    @java.lang.Override
    public emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoodsOrBuilder getGoodsOrBuilder() {
      return getGoods();
    }

    public static final int BUY_COUNT_FIELD_NUMBER = 10;
    private int buyCount_;
    /**
     * <code>uint32 buy_count = 10;</code>
     * @return The buyCount.
     */
    @java.lang.Override
    public int getBuyCount() {
      return buyCount_;
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
      if (goods_ != null) {
        output.writeMessage(3, getGoods());
      }
      if (buyCount_ != 0) {
        output.writeUInt32(10, buyCount_);
      }
      unknownFields.writeTo(output);
    }

    @java.lang.Override
    public int getSerializedSize() {
      int size = memoizedSize;
      if (size != -1) return size;

      size = 0;
      if (goods_ != null) {
        size += com.google.protobuf.CodedOutputStream
          .computeMessageSize(3, getGoods());
      }
      if (buyCount_ != 0) {
        size += com.google.protobuf.CodedOutputStream
          .computeUInt32Size(10, buyCount_);
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
      if (!(obj instanceof emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq)) {
        return super.equals(obj);
      }
      emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq other = (emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq) obj;

      if (hasGoods() != other.hasGoods()) return false;
      if (hasGoods()) {
        if (!getGoods()
            .equals(other.getGoods())) return false;
      }
      if (getBuyCount()
          != other.getBuyCount()) return false;
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
      if (hasGoods()) {
        hash = (37 * hash) + GOODS_FIELD_NUMBER;
        hash = (53 * hash) + getGoods().hashCode();
      }
      hash = (37 * hash) + BUY_COUNT_FIELD_NUMBER;
      hash = (53 * hash) + getBuyCount();
      hash = (29 * hash) + unknownFields.hashCode();
      memoizedHashCode = hash;
      return hash;
    }

    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parseFrom(
        java.nio.ByteBuffer data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parseFrom(
        java.nio.ByteBuffer data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data);
    }
    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return PARSER.parseFrom(data, extensionRegistry);
    }
    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input, extensionRegistry);
    }
    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input);
    }
    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseDelimitedWithIOException(PARSER, input, extensionRegistry);
    }
    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return com.google.protobuf.GeneratedMessageV3
          .parseWithIOException(PARSER, input);
    }
    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parseFrom(
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
    public static Builder newBuilder(emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq prototype) {
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
     * CmdId: 4760
     * EnetChannelId: 0
     * EnetIsReliable: true
     * IsAllowClient: true
     * </pre>
     *
     * Protobuf type {@code HomeLimitedShopBuyGoodsReq}
     */
    public static final class Builder extends
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
        // @@protoc_insertion_point(builder_implements:HomeLimitedShopBuyGoodsReq)
        emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReqOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.internal_static_HomeLimitedShopBuyGoodsReq_descriptor;
      }

      @java.lang.Override
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.internal_static_HomeLimitedShopBuyGoodsReq_fieldAccessorTable
            .ensureFieldAccessorsInitialized(
                emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq.class, emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq.Builder.class);
      }

      // Construct using emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq.newBuilder()
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
        if (goodsBuilder_ == null) {
          goods_ = null;
        } else {
          goods_ = null;
          goodsBuilder_ = null;
        }
        buyCount_ = 0;

        return this;
      }

      @java.lang.Override
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.internal_static_HomeLimitedShopBuyGoodsReq_descriptor;
      }

      @java.lang.Override
      public emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq getDefaultInstanceForType() {
        return emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq.getDefaultInstance();
      }

      @java.lang.Override
      public emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq build() {
        emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }

      @java.lang.Override
      public emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq buildPartial() {
        emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq result = new emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq(this);
        if (goodsBuilder_ == null) {
          result.goods_ = goods_;
        } else {
          result.goods_ = goodsBuilder_.build();
        }
        result.buyCount_ = buyCount_;
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
        if (other instanceof emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq) {
          return mergeFrom((emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }

      public Builder mergeFrom(emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq other) {
        if (other == emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq.getDefaultInstance()) return this;
        if (other.hasGoods()) {
          mergeGoods(other.getGoods());
        }
        if (other.getBuyCount() != 0) {
          setBuyCount(other.getBuyCount());
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
        emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq parsedMessage = null;
        try {
          parsedMessage = PARSER.parsePartialFrom(input, extensionRegistry);
        } catch (com.google.protobuf.InvalidProtocolBufferException e) {
          parsedMessage = (emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq) e.getUnfinishedMessage();
          throw e.unwrapIOException();
        } finally {
          if (parsedMessage != null) {
            mergeFrom(parsedMessage);
          }
        }
        return this;
      }

      private emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods goods_;
      private com.google.protobuf.SingleFieldBuilderV3<
          emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods, emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods.Builder, emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoodsOrBuilder> goodsBuilder_;
      /**
       * <code>.HomeLimitedShopGoods goods = 3;</code>
       * @return Whether the goods field is set.
       */
      public boolean hasGoods() {
        return goodsBuilder_ != null || goods_ != null;
      }
      /**
       * <code>.HomeLimitedShopGoods goods = 3;</code>
       * @return The goods.
       */
      public emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods getGoods() {
        if (goodsBuilder_ == null) {
          return goods_ == null ? emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods.getDefaultInstance() : goods_;
        } else {
          return goodsBuilder_.getMessage();
        }
      }
      /**
       * <code>.HomeLimitedShopGoods goods = 3;</code>
       */
      public Builder setGoods(emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods value) {
        if (goodsBuilder_ == null) {
          if (value == null) {
            throw new NullPointerException();
          }
          goods_ = value;
          onChanged();
        } else {
          goodsBuilder_.setMessage(value);
        }

        return this;
      }
      /**
       * <code>.HomeLimitedShopGoods goods = 3;</code>
       */
      public Builder setGoods(
          emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods.Builder builderForValue) {
        if (goodsBuilder_ == null) {
          goods_ = builderForValue.build();
          onChanged();
        } else {
          goodsBuilder_.setMessage(builderForValue.build());
        }

        return this;
      }
      /**
       * <code>.HomeLimitedShopGoods goods = 3;</code>
       */
      public Builder mergeGoods(emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods value) {
        if (goodsBuilder_ == null) {
          if (goods_ != null) {
            goods_ =
              emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods.newBuilder(goods_).mergeFrom(value).buildPartial();
          } else {
            goods_ = value;
          }
          onChanged();
        } else {
          goodsBuilder_.mergeFrom(value);
        }

        return this;
      }
      /**
       * <code>.HomeLimitedShopGoods goods = 3;</code>
       */
      public Builder clearGoods() {
        if (goodsBuilder_ == null) {
          goods_ = null;
          onChanged();
        } else {
          goods_ = null;
          goodsBuilder_ = null;
        }

        return this;
      }
      /**
       * <code>.HomeLimitedShopGoods goods = 3;</code>
       */
      public emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods.Builder getGoodsBuilder() {
        
        onChanged();
        return getGoodsFieldBuilder().getBuilder();
      }
      /**
       * <code>.HomeLimitedShopGoods goods = 3;</code>
       */
      public emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoodsOrBuilder getGoodsOrBuilder() {
        if (goodsBuilder_ != null) {
          return goodsBuilder_.getMessageOrBuilder();
        } else {
          return goods_ == null ?
              emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods.getDefaultInstance() : goods_;
        }
      }
      /**
       * <code>.HomeLimitedShopGoods goods = 3;</code>
       */
      private com.google.protobuf.SingleFieldBuilderV3<
          emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods, emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods.Builder, emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoodsOrBuilder> 
          getGoodsFieldBuilder() {
        if (goodsBuilder_ == null) {
          goodsBuilder_ = new com.google.protobuf.SingleFieldBuilderV3<
              emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods, emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoods.Builder, emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.HomeLimitedShopGoodsOrBuilder>(
                  getGoods(),
                  getParentForChildren(),
                  isClean());
          goods_ = null;
        }
        return goodsBuilder_;
      }

      private int buyCount_ ;
      /**
       * <code>uint32 buy_count = 10;</code>
       * @return The buyCount.
       */
      @java.lang.Override
      public int getBuyCount() {
        return buyCount_;
      }
      /**
       * <code>uint32 buy_count = 10;</code>
       * @param value The buyCount to set.
       * @return This builder for chaining.
       */
      public Builder setBuyCount(int value) {
        
        buyCount_ = value;
        onChanged();
        return this;
      }
      /**
       * <code>uint32 buy_count = 10;</code>
       * @return This builder for chaining.
       */
      public Builder clearBuyCount() {
        
        buyCount_ = 0;
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


      // @@protoc_insertion_point(builder_scope:HomeLimitedShopBuyGoodsReq)
    }

    // @@protoc_insertion_point(class_scope:HomeLimitedShopBuyGoodsReq)
    private static final emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq DEFAULT_INSTANCE;
    static {
      DEFAULT_INSTANCE = new emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq();
    }

    public static emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq getDefaultInstance() {
      return DEFAULT_INSTANCE;
    }

    private static final com.google.protobuf.Parser<HomeLimitedShopBuyGoodsReq>
        PARSER = new com.google.protobuf.AbstractParser<HomeLimitedShopBuyGoodsReq>() {
      @java.lang.Override
      public HomeLimitedShopBuyGoodsReq parsePartialFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws com.google.protobuf.InvalidProtocolBufferException {
        return new HomeLimitedShopBuyGoodsReq(input, extensionRegistry);
      }
    };

    public static com.google.protobuf.Parser<HomeLimitedShopBuyGoodsReq> parser() {
      return PARSER;
    }

    @java.lang.Override
    public com.google.protobuf.Parser<HomeLimitedShopBuyGoodsReq> getParserForType() {
      return PARSER;
    }

    @java.lang.Override
    public emu.grasscutter.net.proto.HomeLimitedShopBuyGoodsReqOuterClass.HomeLimitedShopBuyGoodsReq getDefaultInstanceForType() {
      return DEFAULT_INSTANCE;
    }

  }

  private static final com.google.protobuf.Descriptors.Descriptor
    internal_static_HomeLimitedShopBuyGoodsReq_descriptor;
  private static final 
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
      internal_static_HomeLimitedShopBuyGoodsReq_fieldAccessorTable;

  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static  com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n HomeLimitedShopBuyGoodsReq.proto\032\032Home" +
      "LimitedShopGoods.proto\"U\n\032HomeLimitedSho" +
      "pBuyGoodsReq\022$\n\005goods\030\003 \001(\0132\025.HomeLimite" +
      "dShopGoods\022\021\n\tbuy_count\030\n \001(\rB\026\n\024org.sor" +
      "apointa.protob\006proto3"
    };
    descriptor = com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.getDescriptor(),
        });
    internal_static_HomeLimitedShopBuyGoodsReq_descriptor =
      getDescriptor().getMessageTypes().get(0);
    internal_static_HomeLimitedShopBuyGoodsReq_fieldAccessorTable = new
      com.google.protobuf.GeneratedMessageV3.FieldAccessorTable(
        internal_static_HomeLimitedShopBuyGoodsReq_descriptor,
        new java.lang.String[] { "Goods", "BuyCount", });
    emu.grasscutter.net.proto.HomeLimitedShopGoodsOuterClass.getDescriptor();
  }

  // @@protoc_insertion_point(outer_class_scope)
}

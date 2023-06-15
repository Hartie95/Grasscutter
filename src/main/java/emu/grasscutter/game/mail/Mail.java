package emu.grasscutter.game.mail;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Indexed;
import dev.morphia.annotations.Transient;
import emu.grasscutter.database.DatabaseHelper;
import emu.grasscutter.game.player.Player;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import emu.grasscutter.net.proto.EquipParamOuterClass.EquipParam;
import emu.grasscutter.net.proto.MailCollectStateOuterClass.MailCollectState;
import emu.grasscutter.net.proto.MailDataOuterClass.MailData;
import emu.grasscutter.net.proto.MailItemOuterClass;
import emu.grasscutter.net.proto.MailTextContentOuterClass.MailTextContent;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

@Entity(value = "mail", useDiscriminator = false)
public class Mail {
	@Getter @Id private ObjectId id;
    @Getter @Setter @Indexed private int ownerUid;
    @Getter public MailContent mailContent;
    @Getter public List<MailItem> itemList;
    @Getter public long sendTime;
    @Getter public long expireTime;
    @Getter public int importance;
    @Getter public boolean isRead;
    @Getter public boolean isAttachmentGot;
    @Getter public int stateValue;
    @Transient private boolean shouldDelete;

    public Mail() {
        this(new MailContent(), new ArrayList<MailItem>(), (int) Instant.now().getEpochSecond() + 604800); // TODO: add expire time to send mail command
    }

    public Mail(MailContent mailContent, List<MailItem> itemList, long expireTime) {
        this(mailContent, itemList, expireTime, 0);
    }

    public Mail(MailContent mailContent, List<MailItem> itemList, long expireTime, int importance) {
        this(mailContent, itemList, expireTime, importance, 1);
    }

    public Mail(MailContent mailContent, List<MailItem> itemList, long expireTime, int importance, int state) {
        this.mailContent = mailContent;
        this.itemList = itemList;
        this.sendTime = (int) Instant.now().getEpochSecond();
        this.expireTime = expireTime;
        this.importance = importance; // Starred mail, 0 = No star, 1 = Star.
        this.isRead = false;
        this.isAttachmentGot = false;
        this.stateValue = state; // Different mailboxes, 1 = Default, 3 = Gift-box.
    }

    public MailData toProto(Player player) {
        return MailData.newBuilder()
            .setMailId(player.getMailId(this))
            .setMailTextContent(this.mailContent.toProto())
            .addAllItemList(this.itemList.stream().map(MailItem::toProto).toList())
            .setSendTime((int) this.sendTime)
            .setExpireTime((int) this.expireTime)
            .setImportance(this.importance)
            .setIsRead(this.isRead)
            .setIsAttachmentGot(this.isAttachmentGot)
            .setCollectState(MailCollectState.MAIL_COLLECT_STATE_NOT_COLLECTIBLE)
            .build();
    }

	@Entity
    public static class MailContent {
        public String title;
        public String content;
        public String sender;

        public MailContent() {
            this.title = "";
            this.content = "loading...";
            this.sender = "loading";
        }

        public MailContent(String title, String content) {
            this(title, content, "Server");
        }

        public MailContent(String title, String content, Player sender) {
            this(title, content, sender.getNickname());
        }

        public MailContent(String title, String content, String sender) {
            this.title = title;
            this.content = content;
            this.sender = sender;
        }

        public MailTextContent toProto() {
            return MailTextContent.newBuilder()
                .setTitle(this.title)
                .setContent(this.content)
                .setSender(this.sender)
                .build();
        }
    }

    @Entity
    public static class MailItem {
        public int itemId;
        public int itemCount;
        public int itemLevel;

        public MailItem() {
            this.itemId = 11101;
            this.itemCount = 1;
            this.itemLevel = 1;
        }

        public MailItem(int itemId) {
            this(itemId, 1);
        }

        public MailItem(int itemId, int itemCount) { this(itemId, itemCount, 1); }

        public MailItem(int itemId, int itemCount, int itemLevel) {
            this.itemId = itemId;
            this.itemCount = itemCount;
            this.itemLevel = itemLevel;
        }

        public MailItemOuterClass.MailItem toProto() {
            return MailItemOuterClass.MailItem.newBuilder().setEquipParam(EquipParam.newBuilder()
                    .setItemId(this.itemId)
                    .setItemNum(this.itemCount)
                    .setItemLevel(this.itemLevel)
                    .setPromoteLevel(0)//mock
                    .build())
                .build();
        }
    }

	public void save() {
		if (this.expireTime * 1000 < System.currentTimeMillis()) {
			DatabaseHelper.deleteMail(this);
		} else {
			DatabaseHelper.saveMail(this);
		}
	}
}

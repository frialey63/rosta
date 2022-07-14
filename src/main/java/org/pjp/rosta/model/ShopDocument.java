package org.pjp.rosta.model;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ShopDocument {

    @Id
    private String uuid;

    @Size(max = 50)
    @Pattern(regexp="[A-Za-z ]+")
    @NotNull
    private String title;

    @NotNull
    private String filename;

    public ShopDocument() {
        super();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ShopDocument [uuid=");
        builder.append(uuid);
        builder.append(", title=");
        builder.append(title);
        builder.append(", filename=");
        builder.append(filename);
        builder.append("]");
        return builder.toString();
    }

}

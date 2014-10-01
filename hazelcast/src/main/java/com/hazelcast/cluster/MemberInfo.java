/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.cluster;

import com.hazelcast.instance.MemberImpl;
import com.hazelcast.nio.Address;
import com.hazelcast.nio.ObjectDataInput;
import com.hazelcast.nio.ObjectDataOutput;
import com.hazelcast.nio.serialization.DataSerializable;

import java.io.IOException;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.hazelcast.instance.MemberRole;

public class MemberInfo implements DataSerializable {
    private Address address;
    private String uuid;
    private Map<String, Object> attributes;
    private Set<MemberRole> roles;

    public MemberInfo() {
    }

    public MemberInfo(Address address) {
        this.address = address;
        roles = EnumSet.allOf(MemberRole.class);
    }

    public MemberInfo(MemberImpl member) {
        this(member.getAddress(), member.getUuid(), member.getRoles(), member.getAttributes());
    }

    public MemberInfo(Address address, String uuid, Set<MemberRole> roles, Map<String, Object> attributes) {
        this(address);
        this.uuid = uuid;
        this.roles = roles;
        this.attributes = new HashMap<String, Object>(attributes);
    }

    public Address getAddress() {
        return address;
    }

    public String getUuid() {
        return uuid;
    }

    public Map<String, Object> getAttributes() {
        return attributes;
    }

    public Set<MemberRole> getRoles() {
        return roles;
    }

    @Override
    public void readData(ObjectDataInput in) throws IOException {
        address = new Address();
        address.readData(in);
        if (in.readBoolean()) {
            uuid = in.readUTF();
        }

        roles = EnumSet.noneOf(MemberRole.class);

        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            roles.add(MemberRole.valueOf(in.readInt()));
        }

        size = in.readInt();
        if (size > 0) {
            attributes = new HashMap<String, Object>();
        }
        for (int i = 0; i < size; i++) {
            String key = in.readUTF();
            Object value = in.readObject();
            attributes.put(key, value);
        }
    }

    @Override
    public void writeData(ObjectDataOutput out) throws IOException {
        address.writeData(out);
        boolean hasUuid = uuid != null;
        out.writeBoolean(hasUuid);
        if (hasUuid) {
            out.writeUTF(uuid);
        }

        out.writeInt(roles.size());
        for (MemberRole role : roles) {
            out.writeInt(role.getId());
        }

        out.writeInt(attributes == null ? 0 : attributes.size());
        if (attributes != null) {
            for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                out.writeUTF(entry.getKey());
                out.writeObject(entry.getValue());
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((address == null) ? 0 : address.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        MemberInfo other = (MemberInfo) obj;
        if (address == null) {
            if (other.address != null) {
                return false;
            }
        } else if (!address.equals(other.address)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MemberInfo{"
                + "address=" + address
                + '}';
    }
}

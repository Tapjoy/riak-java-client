/*
 * This file is provided to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.basho.riak.newapi;

import java.io.IOException;

import com.basho.riak.client.raw.Command;
import com.basho.riak.client.raw.DefaultRetrier;
import com.basho.riak.client.raw.RawClient;
import com.basho.riak.client.raw.pbc.PBClient;
import com.basho.riak.newapi.bucket.Bucket;
import com.basho.riak.newapi.bucket.FetchBucket;
import com.basho.riak.newapi.bucket.WriteBucket;
import com.basho.riak.newapi.query.LinkWalk;
import com.basho.riak.newapi.query.MapReduce;

/**
 * @author russell
 * 
 */
public class RiakFactory {

    public static RiakClient pbcClient() throws RiakException {

        try {
            final RawClient client = new PBClient("127.0.0.1", 8087);

            return new RiakClient() {
                public LinkWalk walk(RiakObject startObject) {
                    return null;
                }

                public WriteBucket updateBucket(Bucket b) {
                    WriteBucket op = new WriteBucket(client, b);
                    return op;
                }

                public MapReduce mapReduce() {
                    return null;
                }

                public FetchBucket fetchBucket(String bucketName) {
                    FetchBucket op = new FetchBucket(client, bucketName);
                    return op;
                }

                public WriteBucket createBucket(String bucketName) {
                    WriteBucket op = new WriteBucket(client, bucketName);
                    return op;
                }

                public RiakClient setClientId(final byte[] clientId) throws RiakException {
                    if (clientId == null || clientId.length != 4) {
                        throw new IllegalArgumentException("Client Id must be 4 bytes long");
                    }
                    final byte[] cloned = clientId.clone();
                    new DefaultRetrier().attempt(new Command<Boolean>() {
                        public Boolean execute() throws IOException {
                            client.setClientId(cloned);
                            return true;
                        }
                    }, 3);

                    return this;
                }

                public byte[] generateAndSetClientId() throws RiakException {
                    final byte[] clientId = new DefaultRetrier().attempt(new Command<byte[]>() {
                        public byte[] execute() throws IOException {
                            return client.generateAndSetClientId();
                        }
                    }, 3);
                    
                    return clientId;
                }

                public byte[] getClientId() throws RiakException {
                    final byte[] clientId = new DefaultRetrier().attempt(new Command<byte[]>() {
                        public byte[] execute() throws IOException {
                            return client.getClientId();
                        }
                    }, 3);
                    
                    return clientId;
                }
            };
        } catch (IOException e) {
            throw new RiakException(e);
        }
    }

}

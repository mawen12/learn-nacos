package com.mawen.learn.nacos.naming.raft;

import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.alibaba.fastjson.JSON;
import com.mawen.learn.nacos.naming.misc.HttpClient;
import com.mawen.learn.nacos.naming.misc.NetUtils;
import com.mawen.learn.nacos.naming.misc.UtilsAndCommons;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.Response;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.SortedBag;
import org.apache.commons.collections.bag.TreeBag;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/24
 */
@Slf4j
public class PeerSet {

	private RaftPeer leader = null;

	private static Map<String, RaftPeer> peers = new HashMap<>();

	private static Set<String> sites = new HashSet<>();

	public RaftPeer getLeader() {
		if (UtilsAndCommons.STANDALONE_MODE) {
			return local();
		}
		return leader;
	}

	public Set<String> allSites() {
		return sites;
	}

	public void add(List<String> servers) {
		for (String server : servers) {
			RaftPeer peer = new RaftPeer();
			peer.ip = server;

			peers.put(server, peer);
		}

		if (UtilsAndCommons.STANDALONE_MODE) {
			RaftPeer local = local();
			local.state = RaftPeer.State.LEADER;
			local.voteFor = NetUtils.localIp();
		}
	}

	public void remove(List<String> servers) {
		for (String server : servers) {
			peers.remove(server);
		}
	}

	public RaftPeer update(RaftPeer peer) {
		peers.put(peer.ip, peer);
		return peer;
	}

	public boolean isLeader(String ip) {
		if (UtilsAndCommons.STANDALONE_MODE) {
			return true;
		}

		log.info("leader: {}, ip: {}", leader.ip, ip);

		return StringUtils.equals(leader.ip, ip);
	}

	public Set<String> allServersIncludeMyself() {
		return peers.keySet();
	}

	public Set<String> allServersWithMySelf() {
		Set<String> servers = new HashSet<>(peers.keySet());

		servers.remove(local().ip);

		return servers;
	}

	public Collection<RaftPeer> allPeers() {
		return peers.values();
	}

	public int size() {
		return peers.size();
	}

	public RaftPeer decideLeader(RaftPeer candidate) {
		peers.put(candidate.ip, candidate);

		SortedBag ips = new TreeBag();
		for (RaftPeer peer : peers.values()) {
			if (StringUtils.isEmpty(peer.voteFor)) {
				continue;
			}

			ips.add(peer.voteFor);
		}

		String first = (String) ips.last();
		if (ips.getCount(first) >= majorityCount()) {
			RaftPeer peer = peers.get(first);
			peer.state = RaftPeer.State.LEADER;

			if (!ObjectUtils.equals(leader, peer)) {
				leader = peer;
				log.info("{} has become the LEADER", leader.ip);
			}
		}

		return leader;
	}

	public RaftPeer makeLeader(RaftPeer candidate) {
		if (!ObjectUtils.equals(leader, candidate)) {
			leader = candidate;
			log.info("{} has become the LEADER, local: {}, leader: {}", leader.ip, JSON.toJSONString(local()), JSON.toJSONString(leader));
		}

		for (RaftPeer peer : peers.values()) {
			Map<String, String> params = new HashMap<>(1);
			if (!ObjectUtils.equals(peer, candidate) && peer.state == RaftPeer.State.LEADER) {
				try {
					String url = RaftCore.buildURL(peer.ip, RaftCore.API_GET_PEER);
					HttpClient.asyncHttpPost(url, null, params, new AsyncCompletionHandler() {
						@Override
						public Integer onCompleted(Response response) throws Exception {
							if (response.getStatusCode() != HttpURLConnection.HTTP_OK) {
								log.error("get peer failed: {}, peer: {}", response.getResponseBody(), peer.ip);
								peer.state = RaftPeer.State.FOLLOWER;
								return 1;
							}

							update(JSON.parseObject(response.getResponseBody(), RaftPeer.class));

							return 0;
						}
					});
				}
				catch (Exception e) {
					peer.state = RaftPeer.State.FOLLOWER;
					log.error("error while getting peer from peer: {}", peer.ip, e);
				}
			}
		}

		return update(candidate);
	}

	public RaftPeer local() {
		RaftPeer peer = peers.get(NetUtils.localIp());
		if (peer == null) {
			throw new IllegalStateException("unable to find local peer: " + NetUtils.localIp() + ", all peers: " + Arrays.toString(peers.keySet().toArray()));
		}
		return peer;
	}

	public RaftPeer get(String server) {
		return peers.get(server);
	}

	public int majorityCount() {
		return peers.size() / 2 + 1;
	}

	public void reset() {

		leader = null;

		for (RaftPeer peer : peers.values()) {
			peer.voteFor = null;
		}
	}

	public void setTerm(long term) {
		RaftPeer local = local();

		if (term < local.term.get()) {
			return;
		}

		local.term.set(term);
	}

	public long getTerm() {
		return local().term.get();
	}

	public boolean contains(RaftPeer remote) {
		return peers.containsKey(remote.ip);
	}
}

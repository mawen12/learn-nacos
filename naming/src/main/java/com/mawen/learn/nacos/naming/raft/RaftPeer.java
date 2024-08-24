package com.mawen.learn.nacos.naming.raft;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.RandomUtils;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/24
 */
public class RaftPeer {

	public String ip;

	public String voteFor;

	public AtomicLong term = new AtomicLong(0L);

	public volatile long leaderDueMs = RandomUtils.nextLong(0, GlobalExecutor.LEADER_TIMEOUT_MS);

	public volatile long heartBeatDueMs = RandomUtils.nextLong(0, GlobalExecutor.HEARTBEAT_INTERVAL_MS);

	public State state = State.FOLLOWER;

	public void resetLeaderDue() {
		leaderDueMs = GlobalExecutor.LEADER_TIMEOUT_MS + RandomUtils.nextLong(0, GlobalExecutor.RANDOM_MS);
	}

	public void resetHeartbeatDue() {
		heartBeatDueMs = GlobalExecutor.HEARTBEAT_INTERVAL_MS;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		RaftPeer raftPeer = (RaftPeer) o;
		return Objects.equals(ip, raftPeer.ip);
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(ip);
	}

	public enum State {
		LEADER,

		FOLLOWER,

		CANDIDATE
	}
}

package com.mawen.learn.nacos.client.naming.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 * @since 2024/8/20
 */
public class Chooser<K, T> {

	private K uniqueKey;

	private Ref<T> ref;

	public Chooser(K uniqueKey) {
		this(uniqueKey, new ArrayList<>());
	}

	public Chooser(K uniqueKey, List<Pair<T>> pairs) {
		Ref<T> ref = new Ref<>(pairs);
		ref.refresh();
		this.uniqueKey = uniqueKey;
		this.ref = ref;
	}

	public void refresh(List<Pair<T>> itemsWithWeight) {
		Ref<T> newRef = new Ref<>(itemsWithWeight);
		newRef.refresh();
		newRef.poller = this.ref.poller.refresh(newRef.items);
		this.ref = newRef;
	}

	public K getUniqueKey() {
		return uniqueKey;
	}

	public Ref<T> getRef() {
		return ref;
	}

	public T random() {
		List<T> items = ref.items;
		if (items.size() == 0) {
			return null;
		}
		if (items.size() == 1) {
			return items.get(0);
		}
		return items.get(ThreadLocalRandom.current().nextInt(items.size()));
	}

	public T randomWithWeight() {
		Ref<T> ref = this.ref;
		double random = ThreadLocalRandom.current().nextDouble(0, 1);
		int index = Arrays.binarySearch(ref.weights, random);
		if (index < 0) {
			index = -index - 1;
		}
		else {
			return ref.items.get(index);
		}

		if (index >= 0 && index < ref.weights.length) {
			if (random < ref.weights[index]) {
				return ref.items.get(index);
			}
		}

		return ref.items.get(ref.items.size() - 1);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		Chooser<?, ?> chooser = (Chooser<?, ?>) o;
		return ref == chooser.ref && Objects.equals(uniqueKey, chooser.uniqueKey);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uniqueKey);
	}

	public class Ref<T> {
		private List<Pair<T>> itemsWithWeight = new ArrayList<>();

		private List<T> items = new ArrayList<>();

		private Poller<T> poller = new GenericPoller<>(items);

		private double[] weights;

		public Ref(List<Pair<T>> itemsWithWeight) {
			this.itemsWithWeight = itemsWithWeight;
		}

		public void refresh() {
			Double originWeightSum = (double) 0;

			for (Pair<T> item : itemsWithWeight) {
				double weight = item.weight();
				if (!(weight > 0)) {
					continue;
				}

				items.add(item.item());
				if (Double.isInfinite(weight)) {
					weight = 10000.00;
				}
				if (Double.isNaN(weight)) {
					weight = 1.0D;
				}
				originWeightSum += weight;
			}

			double[] exactWeights = new double[items.size()];
			int index = 0;
			for (Pair<T> item : itemsWithWeight) {
				double singleWeight = item.weight();
				// ignore item which weight is zero. see test_randomWithWeight_weight0 in ChooserTest
				if (!(singleWeight > 0)) {
					continue;
				}
				exactWeights[index++] = singleWeight / originWeightSum;
			}

			weights = new double[items.size()];
			double randomRange = 0D;
			for (int i = 0; i < index; i++) {
				weights[i] = randomRange + exactWeights[i];
				randomRange += exactWeights[i];
			}

			double doublePrecisionDelta = 0.0001;
			if (index != 0 && !(Math.abs(weights[index - 1] - 1) < doublePrecisionDelta)) {
				throw new IllegalStateException("Cumulative Weight calculate wrong, the sum of probabilities does not equal 1.");
			}
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;

			if (o == null || getClass() != o.getClass()) return false;

			Ref<?> ref = (Ref<?>) o;
			return Objects.equals(itemsWithWeight, ref.itemsWithWeight);
		}

		@Override
		public int hashCode() {
			return itemsWithWeight.hashCode();
		}
	}
}

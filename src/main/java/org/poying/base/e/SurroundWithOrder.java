package org.poying.base.e;

import org.poying.base.ext.Surround;

/**
 * 用于包装Surround和其执行顺序的内部类
 *
 * @author poying
 */
public record SurroundWithOrder(Surround surround, int order) {
}

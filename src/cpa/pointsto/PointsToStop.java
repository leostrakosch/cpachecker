/**
 *
 */
package cpa.pointsto;

import java.util.Collection;

import cpa.common.interfaces.AbstractDomain;
import cpa.common.interfaces.AbstractElement;
import cpa.common.interfaces.Precision;
import cpa.common.interfaces.StopOperator;
import exceptions.CPAException;

/**
 * @author Michael Tautschnig <tautschnig@forsyte.de>
 *
 */
public class PointsToStop implements StopOperator {

	private final AbstractDomain abstractDomain;

	public PointsToStop (AbstractDomain abstractDomain) {
		this.abstractDomain = abstractDomain;
	}

	/* (non-Javadoc)
	 * @see cpa.common.interfaces.StopOperator#stop(cpa.common.interfaces.AbstractElement, java.util.Collection)
	 */
	public <AE extends AbstractElement> boolean stop(AE element,
			Collection<AE> reached, Precision prec) throws CPAException {
		for (AbstractElement r : reached) {
			if (stop(element, r)) return true;
		}
		return false;
	}

	/* (non-Javadoc)
	 * @see cpa.common.interfaces.StopOperator#stop(cpa.common.interfaces.AbstractElement, cpa.common.interfaces.AbstractElement)
	 */
	public boolean stop(AbstractElement element, AbstractElement reachedElement)
			throws CPAException {
		return abstractDomain.getPartialOrder().satisfiesPartialOrder(element, reachedElement);
	}

}

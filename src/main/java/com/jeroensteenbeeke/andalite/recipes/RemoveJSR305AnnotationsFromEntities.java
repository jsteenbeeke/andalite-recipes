package com.jeroensteenbeeke.andalite.recipes;

import java.util.HashMap;

import com.jeroensteenbeeke.andalite.core.ActionResult;
import com.jeroensteenbeeke.andalite.forge.AbstractForgeRecipe;
import com.jeroensteenbeeke.andalite.forge.ForgeException;
import com.jeroensteenbeeke.andalite.forge.ui.Action;
import com.jeroensteenbeeke.andalite.recipes.jsr305.RemoveEntityJSR305;

public class RemoveJSR305AnnotationsFromEntities extends AbstractForgeRecipe {

	public RemoveJSR305AnnotationsFromEntities() {
		super("Remove JSR305 annotations from entities", new HashMap<>());
	}

	public ActionResult checkCorrectlyConfigured() {
		return ActionResult.ok();
	}

	public Action onSelected() throws ForgeException {
		return new RemoveEntityJSR305();
	}

}

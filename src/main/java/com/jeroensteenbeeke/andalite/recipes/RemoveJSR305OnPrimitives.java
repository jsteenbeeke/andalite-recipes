package com.jeroensteenbeeke.andalite.recipes;

import java.util.HashMap;

import com.jeroensteenbeeke.andalite.core.ActionResult;
import com.jeroensteenbeeke.andalite.forge.AbstractForgeRecipe;
import com.jeroensteenbeeke.andalite.forge.ForgeException;
import com.jeroensteenbeeke.andalite.forge.ui.Action;
import com.jeroensteenbeeke.andalite.recipes.jsr305.CheckForPrimitivesWithJSR305;
import com.jeroensteenbeeke.andalite.recipes.jsr305.ScanAndTransform;

public class RemoveJSR305OnPrimitives extends AbstractForgeRecipe {
	
	public RemoveJSR305OnPrimitives() {
		super("Remove JSR305 annotations from primitives", new HashMap<>());
	}

	public ActionResult checkCorrectlyConfigured() {
		return ActionResult.ok();
	}

	public Action onSelected() throws ForgeException {
		return new CheckForPrimitivesWithJSR305();
	}

}

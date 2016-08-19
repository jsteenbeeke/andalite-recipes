package com.jeroensteenbeeke.andalite.recipes;

import java.util.HashMap;

import com.jeroensteenbeeke.andalite.core.ActionResult;
import com.jeroensteenbeeke.andalite.forge.AbstractForgeRecipe;
import com.jeroensteenbeeke.andalite.forge.ForgeException;
import com.jeroensteenbeeke.andalite.forge.ui.Action;
import com.jeroensteenbeeke.andalite.recipes.jsr305.AddJSR305Annotations;

public class JSR305Transformation extends AbstractForgeRecipe {
	

	public JSR305Transformation() {
		super("Add JSR305 annotations to entities", new HashMap<>());
	}

	public ActionResult checkCorrectlyConfigured() {
		return ActionResult.ok();
	}

	public Action onSelected() throws ForgeException {
		return new AddJSR305Annotations();
	}

}

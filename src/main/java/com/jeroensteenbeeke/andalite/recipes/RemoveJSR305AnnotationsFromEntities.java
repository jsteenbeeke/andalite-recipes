package com.jeroensteenbeeke.andalite.recipes;

import java.util.HashMap;

import com.jeroensteenbeeke.andalite.forge.AbstractForgeRecipe;
import com.jeroensteenbeeke.andalite.forge.ui.Action;
import com.jeroensteenbeeke.andalite.forge.ui.questions.Answers;
import com.jeroensteenbeeke.andalite.forge.ui.questions.templates.QuestionTemplate;
import com.jeroensteenbeeke.andalite.forge.ui.questions.templates.Questions;
import com.jeroensteenbeeke.andalite.recipes.jsr305.RemoveEntityJSR305;
import com.jeroensteenbeeke.lux.ActionResult;
import org.jetbrains.annotations.NotNull;

public class RemoveJSR305AnnotationsFromEntities extends AbstractForgeRecipe
{

	public RemoveJSR305AnnotationsFromEntities()
	{
		super("Remove JSR305 annotations from entities", new HashMap<>());
	}

	@NotNull
	public ActionResult checkCorrectlyConfigured()
	{
		return ActionResult.ok();
	}

	@Override
	@NotNull
	public Action createAction(@NotNull Answers answers)
	{
		return new RemoveEntityJSR305();
	}

	@Override
	@NotNull
	public QuestionTemplate< ? , ? > getInitialQuestion()
	{
		return Questions.none();
	}
}

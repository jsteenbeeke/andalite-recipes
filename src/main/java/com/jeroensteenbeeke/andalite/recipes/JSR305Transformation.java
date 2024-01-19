package com.jeroensteenbeeke.andalite.recipes;

import java.util.HashMap;

import com.jeroensteenbeeke.andalite.forge.AbstractForgeRecipe;
import com.jeroensteenbeeke.andalite.forge.ui.Action;
import com.jeroensteenbeeke.andalite.forge.ui.questions.Answers;
import com.jeroensteenbeeke.andalite.forge.ui.questions.templates.QuestionTemplate;
import com.jeroensteenbeeke.andalite.forge.ui.questions.templates.Questions;
import com.jeroensteenbeeke.andalite.recipes.jsr305.AddJSR305Annotations;
import com.jeroensteenbeeke.lux.ActionResult;
import org.jetbrains.annotations.NotNull;

public class JSR305Transformation extends AbstractForgeRecipe
{

	public JSR305Transformation()
	{
		super("Add JSR305 annotations to entities", new HashMap<>());
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
		return new AddJSR305Annotations();
	}

	@Override
	@NotNull
	public QuestionTemplate< ? , ? > getInitialQuestion()
	{
		return Questions.none();
	}
}

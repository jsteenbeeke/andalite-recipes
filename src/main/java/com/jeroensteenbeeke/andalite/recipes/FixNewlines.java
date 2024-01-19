package com.jeroensteenbeeke.andalite.recipes;

import java.util.HashMap;

import com.jeroensteenbeeke.andalite.forge.AbstractForgeRecipe;
import com.jeroensteenbeeke.andalite.forge.ui.Action;
import com.jeroensteenbeeke.andalite.forge.ui.questions.Answers;
import com.jeroensteenbeeke.andalite.forge.ui.questions.templates.QuestionTemplate;
import com.jeroensteenbeeke.andalite.forge.ui.questions.templates.Questions;
import com.jeroensteenbeeke.andalite.recipes.jsr305.ScanAndRemoveNewlines;
import com.jeroensteenbeeke.lux.ActionResult;
import org.jetbrains.annotations.NotNull;

public class FixNewlines extends AbstractForgeRecipe
{

	public FixNewlines()
	{
		super("Remove unnecessary newlines", new HashMap<>());
	}

	@Override
	@NotNull
	public ActionResult checkCorrectlyConfigured()
	{
		return ActionResult.ok();
	}

	@Override
	@NotNull
	public Action createAction(@NotNull Answers answers)
	{
		return new ScanAndRemoveNewlines();
	}

	@Override
	@NotNull
	public QuestionTemplate< ? , ? > getInitialQuestion()
	{
		return Questions.none();
	}
}

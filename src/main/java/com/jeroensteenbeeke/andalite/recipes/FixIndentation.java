package com.jeroensteenbeeke.andalite.recipes;

import java.util.HashMap;

import com.jeroensteenbeeke.andalite.forge.AbstractForgeRecipe;
import com.jeroensteenbeeke.andalite.forge.ForgeException;
import com.jeroensteenbeeke.andalite.forge.ui.Action;
import com.jeroensteenbeeke.andalite.forge.ui.questions.Answers;
import com.jeroensteenbeeke.andalite.forge.ui.questions.templates.QuestionTemplate;
import com.jeroensteenbeeke.andalite.forge.ui.questions.templates.Questions;
import com.jeroensteenbeeke.andalite.recipes.jsr305.ScanAndRemoveFunkyIndentation;
import com.jeroensteenbeeke.lux.ActionResult;
import org.jetbrains.annotations.NotNull;

public class FixIndentation extends AbstractForgeRecipe
{

	public FixIndentation()
	{
		super("Remove unnecessary indentation in setters", new HashMap<>());
	}

	@NotNull
	public ActionResult checkCorrectlyConfigured()
	{
		return ActionResult.ok();
	}

	@Override
	public @NotNull Action createAction(@NotNull Answers answers)
	{
		return new ScanAndRemoveFunkyIndentation();
	}

	@Override
	@NotNull
	public QuestionTemplate< ? , ? > getInitialQuestion()
	{
		return Questions.none();
	}
}
